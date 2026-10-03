package com.grtc.main.qna.complain;

import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.Pages;
import com.grtc.main.global.common.SearchUtil;
import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.service.LoginService;
import com.grtc.main.qna.complain.dto.ComplainDto;
import com.grtc.main.qna.file.Attachment;
import com.grtc.main.qna.file.FileService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 민원 등록/조회/수정/삭제, 첨부파일 다운로드 권한 확인을 담당하는 서비스 (사용자 화면)
//  - 일반 사용자: 목록은 전체(남의 민원인 이름은 가림), 상세/수정/삭제는 본인이 쓴 민원만
//  - 관리자의 전체 민원 관리와 답변 등록은 dashboard 서버(/api/admin/complaints)에서 처리한다.
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplainService {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final ComplainRepository complainRepository;
    private final LoginService loginService;
    private final FileService fileService;

    // =========================================================
    // 목록
    // =========================================================

    // 민원 목록 (사용자 민원관리 화면)
    //  - 목록은 전체 민원을 보여준다. 다만 상세(내용, 첨부파일)는 본인 것만 열린다.
    //  - 본인 글인지는 ListItem.mine 으로 알려준다.
    public PageResponse<ComplainDto.ListItem> listForUser(Long userId, ComplainDto.SearchCondition condition,
                                                          int page, int size) {
        LoginEntity me = loginService.getActiveMember(userId);
        return search(me.getId(), condition, page, size, true);
    }

    // viewerId: 보는 사람 (목록의 mine 표시에만 사용, 조회 범위는 제한하지 않는다)
    // maskOthers: true 면 남의 민원인 이름을 가린다 (일반 사용자 목록)
    private PageResponse<ComplainDto.ListItem> search(Long viewerId, ComplainDto.SearchCondition condition,
                                                      int page, int size, boolean maskOthers) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt", "id"); // 최신순
        Page<ComplainEntity> result = complainRepository.findAll(spec(condition), Pages.of(page, size, sort));
        return PageResponse.of(result, c -> ComplainDto.ListItem.from(c, viewerId, maskOthers));
    }

    // =========================================================
    // 상세
    // =========================================================

    // 민원 상세 (일반 사용자는 본인 것만, 관리자는 전체)
    public ComplainDto.Detail get(Long userId, Long id) {
        LoginEntity me = loginService.getActiveMember(userId);
        return toDetail(findAccessible(me, id), me);
    }

    // =========================================================
    // 등록 / 수정 / 삭제
    // =========================================================

    // 민원 등록 (첨부파일 포함). 첨부파일 저장이 실패하면 민원 등록도 함께 취소된다.
    @Transactional
    public ComplainDto.Detail create(Long userId, ComplainDto.SaveRequest request, List<MultipartFile> files) {
        LoginEntity me = loginService.getActiveMember(userId);

        // 민원번호: CM + 오늘(yyMMdd) + - + 그날의 일련번호(3자리)
        String prefix = "CM" + LocalDate.now().format(NO_DATE) + "-";
        long sequence = complainRepository.countByComplainNoStartingWith(prefix) + 1;

        ComplainEntity saved = complainRepository.save(ComplainEntity.builder()
                .complainNo(prefix + String.format("%03d", sequence))
                .type(request.type())
                .category(categoryOrDefault(request.category()))
                .title(request.title().trim())
                .content(request.content().trim())
                .writer(me)
                .build());

        fileService.store(saved, files);
        log.info("[complain] 등록 id={} no={} writerId={}", saved.getId(), saved.getComplainNo(), me.getId());
        return toDetail(saved, me);
    }

    // 민원 수정: 작성자 본인 + 접수대기 상태에서만. 기존 파일 삭제와 새 파일 추가를 함께 처리한다.
    @Transactional
    public ComplainDto.Detail update(Long userId, Long id, ComplainDto.SaveRequest request,
                                     List<MultipartFile> files) {
        LoginEntity me = loginService.getActiveMember(userId);
        ComplainEntity complain = findAccessible(me, id);
        requireOwner(me, complain);
        requireWaiting(complain);

        // 지울 파일 번호가 이 민원의 첨부파일이 맞는지 확인 (다른 민원의 파일을 지우지 못하게)
        List<Attachment> existing = fileService.findByComplain(complain.getId());
        Set<Long> deleteIds = request.deleteFileIds() == null
                ? Set.of() : new HashSet<>(request.deleteFileIds());
        List<Attachment> toDelete = existing.stream()
                .filter(a -> deleteIds.contains(a.getId()))
                .toList();
        if (toDelete.size() != deleteIds.size()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 남는 파일 + 새 파일이 최대 개수를 넘지 않는지 확인
        long newCount = files == null ? 0 : files.stream().filter(f -> !f.isEmpty()).count();
        if (existing.size() - toDelete.size() + newCount > FileService.MAX_FILE_COUNT) {
            throw new BusinessException(ErrorCode.FILE_TOO_MANY);
        }

        // 삭제 -> 추가 순서: 추가가 실패해 롤백되면 기존 파일은 디스크에서 지워지지 않는다.
        toDelete.forEach(a -> fileService.delete(a.getId()));
        fileService.store(complain, files);

        complain.update(request.type(), categoryOrDefault(request.category()),
                request.title().trim(), request.content().trim());
        log.info("[complain] 수정 id={}", id);
        return toDetail(complain, me);
    }

    // 민원 삭제: 작성자 본인 + 접수대기 상태에서만. 딸린 첨부파일도 함께 지운다.
    @Transactional
    public void delete(Long userId, Long id) {
        LoginEntity me = loginService.getActiveMember(userId);
        ComplainEntity complain = findAccessible(me, id);
        requireOwner(me, complain);
        requireWaiting(complain);

        fileService.findByComplain(id).forEach(a -> fileService.delete(a.getId()));
        complainRepository.delete(complain);
        log.info("[complain] 삭제 id={}", id);
    }

    // =========================================================
    // 첨부파일 다운로드 권한 확인
    // =========================================================

    // 민원을 볼 수 있는 사람(작성자 본인, 관리자)만 + 그 민원에 속한 파일만 내려받을 수 있다.
    public Attachment getAttachmentForDownload(Long userId, Long complainId, Long attachmentId) {
        LoginEntity me = loginService.getActiveMember(userId);
        findAccessible(me, complainId);

        Attachment attachment = fileService.getAttachment(attachmentId);
        if (!attachment.getComplain().getId().equals(complainId)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return attachment;
    }

    // =========================================================
    // 선택 목록
    // =========================================================

    public ComplainDto.Options options() {
        return new ComplainDto.Options(
                Arrays.stream(ComplainType.values())
                        .map(t -> new ComplainDto.LabelValue(t.name(), t.getLabel())).toList(),
                Arrays.stream(ComplainCategory.values())
                        .map(c -> new ComplainDto.LabelValue(c.name(), c.getLabel())).toList(),
                Arrays.stream(ComplainStatus.values())
                        .map(s -> new ComplainDto.LabelValue(s.name(), s.getLabel())).toList()
        );
    }

    // =========================================================
    // 내부 도우미
    // =========================================================

    // 검색 조건 조립: 검색어, 분류, 유형, 처리상태
    private Specification<ComplainEntity> spec(ComplainDto.SearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (condition != null) {
                if (SearchUtil.hasText(condition.keyword())) {
                    String like = SearchUtil.contains(condition.keyword());
                    Join<ComplainEntity, LoginEntity> writer = root.join("writer");
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.<String>get("title")), like, SearchUtil.ESCAPE),
                            cb.like(cb.lower(root.<String>get("content")), like, SearchUtil.ESCAPE),
                            cb.like(cb.lower(root.<String>get("complainNo")), like, SearchUtil.ESCAPE),
                            cb.like(cb.lower(writer.<String>get("name")), like, SearchUtil.ESCAPE)
                    ));
                }
                if (condition.category() != null) {
                    predicates.add(cb.equal(root.get("category"), condition.category()));
                }
                if (condition.type() != null) {
                    predicates.add(cb.equal(root.get("type"), condition.type()));
                }
                if (condition.status() != null) {
                    predicates.add(cb.equal(root.get("status"), condition.status()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // 볼 수 있는 민원만 조회: 관리자는 전부, 일반 사용자는 본인 것만 (남의 것은 '없는 민원'으로 응답)
    private ComplainEntity findAccessible(LoginEntity me, Long id) {
        ComplainEntity complain = complainRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPLAIN_NOT_FOUND));
        if (me.getRole() != Role.ADMIN && !complain.getWriter().getId().equals(me.getId())) {
            throw new BusinessException(ErrorCode.COMPLAIN_NOT_FOUND);
        }
        return complain;
    }

    private void requireOwner(LoginEntity me, ComplainEntity complain) {
        if (!complain.getWriter().getId().equals(me.getId())) {
            throw new BusinessException(ErrorCode.COMPLAIN_NOT_OWNER);
        }
    }

    private void requireWaiting(ComplainEntity complain) {
        if (complain.getStatus() != ComplainStatus.WAITING) {
            throw new BusinessException(ErrorCode.COMPLAIN_NOT_MODIFIABLE);
        }
    }

    private ComplainCategory categoryOrDefault(ComplainCategory category) {
        return category != null ? category : ComplainCategory.ETC;
    }

    private ComplainDto.Detail toDetail(ComplainEntity c, LoginEntity viewer) {
        List<ComplainDto.AttachmentInfo> attachments = fileService.findByComplain(c.getId()).stream()
                .map(a -> ComplainDto.AttachmentInfo.of(c.getId(), a))
                .toList();

        ComplainDto.AnswerInfo answer = c.getAnswerContent() == null ? null
                : new ComplainDto.AnswerInfo(
                        c.getAnswerContent(),
                        c.getAnsweredAt(),
                        c.getAnsweredBy() == null ? null : c.getAnsweredBy().getName());

        boolean editable = c.getWriter().getId().equals(viewer.getId())
                && c.getStatus() == ComplainStatus.WAITING;

        return new ComplainDto.Detail(
                c.getId(),
                c.getComplainNo(),
                c.getType(),
                c.getType().getLabel(),
                c.getCategory(),
                c.getCategory().getLabel(),
                c.getTitle(),
                c.getContent(),
                c.getWriter().getId(),
                c.getWriter().getName(),
                c.getStatus(),
                c.getStatus().getLabel(),
                c.getStatus().getProgressLabel(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                attachments,
                answer,
                editable
        );
    }
}
