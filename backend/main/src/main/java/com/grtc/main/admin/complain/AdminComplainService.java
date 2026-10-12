package com.grtc.main.admin.complain;

import com.grtc.main.qna.complain.ComplainCategory;
import com.grtc.main.qna.complain.ComplainEntity;
import com.grtc.main.qna.complain.ComplainNoGenerator;
import com.grtc.main.qna.complain.ComplainRepository;
import com.grtc.main.qna.complain.ComplainStatus;
import com.grtc.main.qna.complain.ComplainType;
import com.grtc.main.qna.file.Attachment;
import com.grtc.main.qna.file.FileService;
import com.grtc.main.global.common.HtmlSanitizer;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.Pages;
import com.grtc.main.global.common.SearchUtil;
import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.global.security.AdminMemberService;
import com.grtc.main.login.entity.LoginEntity;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminComplainService {

    private final ComplainRepository complainRepository;
    private final ComplainNoGenerator complainNoGenerator;
    private final AdminMemberService adminMemberService;
    private final FileService fileService;

    public PageResponse<AdminComplainDto.ListItem> list(Long adminId, AdminComplainDto.SearchCondition condition,
                                                   int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt", "id");
        Page<ComplainEntity> result = complainRepository.findAll(spec(condition), Pages.of(page, size, sort));
        return PageResponse.of(result, c -> AdminComplainDto.ListItem.from(c, adminId, false));
    }

    public AdminComplainDto.Detail get(Long adminId, Long id) {
        return toDetail(find(id), adminId);
    }

    @Transactional
    public AdminComplainDto.Detail create(Long adminId, AdminComplainDto.SaveRequest request, List<MultipartFile> files) {
        LoginEntity admin = adminMemberService.getActiveAdmin(adminId);

        ComplainEntity saved = complainRepository.save(ComplainEntity.builder()
                .complainNo(complainNoGenerator.next())
                .type(request.type())
                .category(request.category() != null ? request.category() : ComplainCategory.ETC)
                .title(request.title().trim())
                .content(request.content().trim())
                .contentHtml(HtmlSanitizer.clean(request.contentHtml()))
                .writer(admin)
                .build());

        fileService.store(saved, files);
        log.info("[complain] 관리자 등록 id={} no={} adminId={}", saved.getId(), saved.getComplainNo(), adminId);
        return toDetail(saved, adminId);
    }

    @Transactional
    public AdminComplainDto.Detail answer(Long adminId, Long id, AdminComplainDto.AnswerRequest request) {
        LoginEntity admin = adminMemberService.getActiveAdmin(adminId);

        if (request.status() == ComplainStatus.WAITING || request.status() == ComplainStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.INVALID_ANSWER_STATUS);
        }

        ComplainEntity complain = find(id);
        if (complain.isWithdrawn()) {
            throw new BusinessException(ErrorCode.COMPLAIN_WITHDRAWN);
        }
        complain.answer(request.status(), request.content().trim(), admin);
        log.info("[complain] 답변 id={} status={} adminId={}", id, request.status(), adminId);
        return toDetail(complain, adminId);
    }

    public Attachment getAttachmentForDownload(Long complainId, Long attachmentId) {
        find(complainId);
        Attachment attachment = fileService.getAttachment(attachmentId);
        if (!attachment.getComplain().getId().equals(complainId)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return attachment;
    }

    public AdminComplainDto.Options options() {
        return new AdminComplainDto.Options(
                Arrays.stream(ComplainType.values())
                        .map(t -> new AdminComplainDto.LabelValue(t.name(), t.getLabel())).toList(),
                Arrays.stream(ComplainCategory.values())
                        .map(c -> new AdminComplainDto.LabelValue(c.name(), c.getLabel())).toList(),
                Arrays.stream(ComplainStatus.values())
                        .map(s -> new AdminComplainDto.LabelValue(s.name(), s.getLabel())).toList()
        );
    }

    private Specification<ComplainEntity> spec(AdminComplainDto.SearchCondition condition) {
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

    private ComplainEntity find(Long id) {
        return complainRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPLAIN_NOT_FOUND));
    }

    private AdminComplainDto.Detail toDetail(ComplainEntity c, Long viewerId) {
        List<AdminComplainDto.AttachmentInfo> attachments = fileService.findByComplain(c.getId()).stream()
                .map(a -> AdminComplainDto.AttachmentInfo.of(c.getId(), a))
                .toList();

        AdminComplainDto.AnswerInfo answer = c.getAnswerContent() == null ? null
                : new AdminComplainDto.AnswerInfo(
                        c.getAnswerContent(),
                        c.getAnsweredAt(),
                        c.getAnsweredBy() == null ? null : c.getAnsweredBy().getName());

        boolean editable = c.getWriter().getId().equals(viewerId)
                && c.getStatus() == ComplainStatus.WAITING;

        return new AdminComplainDto.Detail(
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
                editable,
                c.getWithdrawnAt()
        );
    }
}
