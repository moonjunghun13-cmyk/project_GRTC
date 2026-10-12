package com.grtc.main.qna.complain;

import com.grtc.main.global.common.HtmlSanitizer;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplainService {

    private final ComplainRepository complainRepository;
    private final ComplainNoGenerator complainNoGenerator;
    private final LoginService loginService;
    private final FileService fileService;

    // 민원 목록 (사용자 민원관리 화면)
    public PageResponse<ComplainDto.ListItem> listForUser(Long userId, ComplainDto.SearchCondition condition,
                                                          int page, int size) {
        LoginEntity me = loginService.getActiveMember(userId);
        return search(me.getId(), condition, page, size, true);
    }

    private PageResponse<ComplainDto.ListItem> search(Long viewerId, ComplainDto.SearchCondition condition,
                                                      int page, int size, boolean maskOthers) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt", "id");
        Page<ComplainEntity> result = complainRepository.findAll(spec(condition), Pages.of(page, size, sort));
        return PageResponse.of(result, c -> ComplainDto.ListItem.from(c, viewerId, maskOthers));
    }

    // 민원 상세 (일반 사용자는 본인 것만, 관리자는 전체)
    public ComplainDto.Detail get(Long userId, Long id) {
        LoginEntity me = loginService.getActiveMember(userId);
        return toDetail(findAccessible(me, id), me);
    }

    @Transactional
    public ComplainDto.Detail create(Long userId, ComplainDto.SaveRequest request, List<MultipartFile> files) {
        LoginEntity me = loginService.getActiveMember(userId);

        ComplainEntity saved = complainRepository.save(ComplainEntity.builder()
                .complainNo(complainNoGenerator.next())
                .type(request.type())
                .category(categoryOrDefault(request.category()))
                .title(request.title().trim())
                .content(request.content().trim())
                .contentHtml(HtmlSanitizer.clean(request.contentHtml()))
                .writer(me)
                .build());

        fileService.store(saved, files);
        log.info("[complain] 등록 id={} no={} writerId={}", saved.getId(), saved.getComplainNo(), me.getId());
        return toDetail(saved, me);
    }

    @Transactional
    public ComplainDto.Detail update(Long userId, Long id, ComplainDto.SaveRequest request,
                                     List<MultipartFile> files) {
        LoginEntity me = loginService.getActiveMember(userId);
        ComplainEntity complain = findAccessible(me, id);
        requireOwner(me, complain);
        requireWaiting(complain);

        List<Attachment> existing = fileService.findByComplain(complain.getId());
        Set<Long> deleteIds = request.deleteFileIds() == null
                ? Set.of() : new HashSet<>(request.deleteFileIds());
        List<Attachment> toDelete = existing.stream()
                .filter(a -> deleteIds.contains(a.getId()))
                .toList();
        if (toDelete.size() != deleteIds.size()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        long newCount = files == null ? 0 : files.stream().filter(f -> !f.isEmpty()).count();
        if (existing.size() - toDelete.size() + newCount > FileService.MAX_FILE_COUNT) {
            throw new BusinessException(ErrorCode.FILE_TOO_MANY);
        }

        toDelete.forEach(a -> fileService.delete(a.getId()));
        fileService.store(complain, files);

        complain.update(request.type(), categoryOrDefault(request.category()),
                request.title().trim(), request.content().trim(),
                HtmlSanitizer.clean(request.contentHtml()));
        log.info("[complain] 수정 id={}", id);
        return toDetail(complain, me);
    }

    // 민원 삭제(= 철회): 작성자 본인 + 접수대기 상태에서만.
    @Transactional
    public void delete(Long userId, Long id) {
        LoginEntity me = loginService.getActiveMember(userId);
        ComplainEntity complain = findAccessible(me, id);
        requireOwner(me, complain);
        requireWaiting(complain);

        complain.withdraw();
        log.info("[complain] 철회 id={} writerId={}", id, me.getId());
    }

    public Attachment getAttachmentForDownload(Long userId, Long complainId, Long attachmentId) {
        LoginEntity me = loginService.getActiveMember(userId);
        findAccessible(me, complainId);

        Attachment attachment = fileService.getAttachment(attachmentId);
        if (!attachment.getComplain().getId().equals(complainId)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return attachment;
    }

    public ComplainDto.Options options() {
        return new ComplainDto.Options(
                Arrays.stream(ComplainType.values())
                        .map(t -> new ComplainDto.LabelValue(t.name(), t.getLabel())).toList(),
                Arrays.stream(ComplainCategory.values())
                        .map(c -> new ComplainDto.LabelValue(c.name(), c.getLabel())).toList(),
                Arrays.stream(ComplainStatus.values())
                        .filter(s -> s != ComplainStatus.WITHDRAWN)
                        .map(s -> new ComplainDto.LabelValue(s.name(), s.getLabel())).toList()
        );
    }

    // 검색 조건 조립: 검색어, 분류, 유형, 처리상태
    private Specification<ComplainEntity> spec(ComplainDto.SearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.notEqual(root.get("status"), ComplainStatus.WITHDRAWN));

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

    private ComplainEntity findAccessible(LoginEntity me, Long id) {
        ComplainEntity complain = complainRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPLAIN_NOT_FOUND));
        if (complain.isWithdrawn()) {
            throw new BusinessException(ErrorCode.COMPLAIN_NOT_FOUND);
        }
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
                c.getContentHtml(),
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
