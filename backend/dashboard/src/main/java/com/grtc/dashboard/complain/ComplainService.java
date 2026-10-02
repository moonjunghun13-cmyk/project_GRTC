package com.grtc.dashboard.complain;

import com.grtc.dashboard.complain.dto.ComplainDto;
import com.grtc.dashboard.complain.file.Attachment;
import com.grtc.dashboard.complain.file.FileService;
import com.grtc.dashboard.global.common.PageResponse;
import com.grtc.dashboard.global.common.Pages;
import com.grtc.dashboard.global.common.SearchUtil;
import com.grtc.dashboard.global.exception.BusinessException;
import com.grtc.dashboard.global.exception.ErrorCode;
import com.grtc.dashboard.global.security.AdminMemberService;
import com.grtc.dashboard.member.entity.MemberEntity;
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
import java.util.List;

// 민원관리(관리자) 비즈니스 로직
//  - 전체 민원 목록/검색, 상세, 답변 등록(처리상태 변경), 관리자 민원 등록, 첨부파일 다운로드
//  - 일반 사용자의 민원 작성/수정/삭제는 main 서버가 담당한다.
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplainService {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final ComplainRepository complainRepository;
    private final AdminMemberService adminMemberService;
    private final FileService fileService;

    // 민원관리 목록 (검색어: 제목/내용/민원인/민원번호, 분류·유형·처리상태 필터, 최신순)
    public PageResponse<ComplainDto.ListItem> list(Long adminId, ComplainDto.SearchCondition condition,
                                                   int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt", "id");
        Page<ComplainEntity> result = complainRepository.findAll(spec(condition), Pages.of(page, size, sort));
        return PageResponse.of(result, c -> ComplainDto.ListItem.from(c, adminId, false));
    }

    // 민원 상세 (관리자는 모든 민원을 볼 수 있다)
    public ComplainDto.Detail get(Long adminId, Long id) {
        return toDetail(find(id), adminId);
    }

    // 관리자 민원 등록 (민원관리 화면의 '+ 민원 등록', 민원인은 로그인한 관리자)
    @Transactional
    public ComplainDto.Detail create(Long adminId, ComplainDto.SaveRequest request, List<MultipartFile> files) {
        MemberEntity admin = adminMemberService.getActiveAdmin(adminId);

        String prefix = "CM" + LocalDate.now().format(NO_DATE) + "-";
        long sequence = complainRepository.countByComplainNoStartingWith(prefix) + 1;

        ComplainEntity saved = complainRepository.save(ComplainEntity.builder()
                .complainNo(prefix + String.format("%03d", sequence))
                .type(request.type())
                .category(request.category() != null ? request.category() : ComplainCategory.ETC)
                .title(request.title().trim())
                .content(request.content().trim())
                .writer(admin)
                .build());

        fileService.store(saved, files);
        log.info("[complain] 관리자 등록 id={} no={} adminId={}", saved.getId(), saved.getComplainNo(), adminId);
        return toDetail(saved, adminId);
    }

    // 답변 등록/수정: 처리상태(답변중/답변완료/이관안내)와 답변 내용을 함께 저장
    @Transactional
    public ComplainDto.Detail answer(Long adminId, Long id, ComplainDto.AnswerRequest request) {
        MemberEntity admin = adminMemberService.getActiveAdmin(adminId);

        // 접수대기로 되돌리는 것은 답변이 아니므로 막는다.
        if (request.status() == ComplainStatus.WAITING) {
            throw new BusinessException(ErrorCode.INVALID_ANSWER_STATUS);
        }

        ComplainEntity complain = find(id);
        complain.answer(request.status(), request.content().trim(), admin);
        log.info("[complain] 답변 id={} status={} adminId={}", id, request.status(), adminId);
        return toDetail(complain, adminId);
    }

    // 첨부파일 다운로드: 그 민원에 속한 파일만
    public Attachment getAttachmentForDownload(Long complainId, Long attachmentId) {
        find(complainId);
        Attachment attachment = fileService.getAttachment(attachmentId);
        if (!attachment.getComplain().getId().equals(complainId)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return attachment;
    }

    // 검색 필터 / 답변 작성의 선택 목록
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

    // ---------- 내부 도우미 ----------

    private Specification<ComplainEntity> spec(ComplainDto.SearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (condition != null) {
                if (SearchUtil.hasText(condition.keyword())) {
                    String like = SearchUtil.contains(condition.keyword());
                    Join<ComplainEntity, MemberEntity> writer = root.join("writer");
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

    private ComplainEntity find(Long id) {
        return complainRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPLAIN_NOT_FOUND));
    }

    private ComplainDto.Detail toDetail(ComplainEntity c, Long viewerId) {
        List<ComplainDto.AttachmentInfo> attachments = fileService.findByComplain(c.getId()).stream()
                .map(a -> ComplainDto.AttachmentInfo.of(c.getId(), a))
                .toList();

        ComplainDto.AnswerInfo answer = c.getAnswerContent() == null ? null
                : new ComplainDto.AnswerInfo(
                        c.getAnswerContent(),
                        c.getAnsweredAt(),
                        c.getAnsweredBy() == null ? null : c.getAnsweredBy().getName());

        // 관리자 화면에서는 민원 내용 수정/삭제 대신 답변을 작성한다. (본인 글 + 접수대기일 때만 true)
        boolean editable = c.getWriter().getId().equals(viewerId)
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
