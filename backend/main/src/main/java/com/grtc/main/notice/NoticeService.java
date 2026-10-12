package com.grtc.main.notice;

import com.grtc.main.global.common.HtmlSanitizer;
import com.grtc.main.global.common.PageResponse;
import com.grtc.main.global.common.SearchUtil;
import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.global.security.AdminMemberService;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

// 공지사항 로직. 조회는 로그인한 회원 누구나, 작성·수정·삭제는 관리자만 (관리자 API 에서만 호출)
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    public static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    public static final Set<String> SORTABLE = Set.of("id", "title", "viewCount", "createdAt");

    private final NoticeRepository noticeRepository;
    private final LoginService loginService;
    private final AdminMemberService adminMemberService;

    // 회원용 목록 (정지·탈퇴 회원은 막는다)
    public PageResponse<NoticeDto.ListItem> listForMember(Long memberId, String keyword, Pageable pageable) {
        loginService.getActiveMember(memberId);
        return list(keyword, pageable);
    }

    // 목록 (회원·관리자 공통). 상단 고정 공지가 항상 먼저 나온다
    public PageResponse<NoticeDto.ListItem> list(String keyword, Pageable pageable) {
        Specification<NoticeEntity> spec = (root, query, cb) -> {
            if (!SearchUtil.hasText(keyword)) {
                return cb.conjunction();
            }
            String like = SearchUtil.contains(keyword);
            return cb.or(
                    cb.like(cb.lower(root.<String>get("title")), like, SearchUtil.ESCAPE),
                    cb.like(cb.lower(root.<String>get("content")), like, SearchUtil.ESCAPE)
            );
        };
        Pageable pinnedFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.desc("pinned")).and(pageable.getSort()));
        return PageResponse.from(noticeRepository.findAll(spec, pinnedFirst), NoticeDto.ListItem::from);
    }

    // 회원용 상세: 볼 때마다 조회수 +1
    @Transactional
    public NoticeDto.Detail getForMember(Long memberId, Long id) {
        loginService.getActiveMember(memberId);
        if (noticeRepository.increaseViewCount(id) == 0) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
        }
        return NoticeDto.Detail.from(find(id));
    }

    // 관리자용 상세: 조회수는 그대로
    public NoticeDto.Detail getForAdmin(Long id) {
        return NoticeDto.Detail.from(find(id));
    }

    @Transactional
    public NoticeDto.Detail create(Long adminId, NoticeDto.CreateRequest request) {
        LoginEntity writer = adminMemberService.getActiveAdmin(adminId);
        NoticeEntity saved = noticeRepository.save(NoticeEntity.builder()
                .title(request.title().trim())
                .content(request.content().trim())
                .contentHtml(HtmlSanitizer.clean(request.contentHtml()))
                .pinned(Boolean.TRUE.equals(request.pinned()))
                .writer(writer)
                .build());
        log.info("[notice] 등록 id={} writerId={}", saved.getId(), writer.getId());
        return NoticeDto.Detail.from(saved);
    }

    @Transactional
    public NoticeDto.Detail update(Long id, NoticeDto.UpdateRequest request) {
        NoticeEntity notice = find(id);
        notice.update(
                request.title() != null ? request.title().trim() : notice.getTitle(),
                request.content() != null ? request.content().trim() : notice.getContent(),
                request.contentHtml() != null ? HtmlSanitizer.clean(request.contentHtml()) : notice.getContentHtml(),
                request.pinned() != null ? request.pinned() : notice.isPinned()
        );
        noticeRepository.flush(); // updatedAt(@PreUpdate)을 응답에 바로 반영
        log.info("[notice] 수정 id={}", id);
        return NoticeDto.Detail.from(notice);
    }

    @Transactional
    public void delete(Long id) {
        noticeRepository.delete(find(id));
        log.info("[notice] 삭제 id={}", id);
    }

    private NoticeEntity find(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
