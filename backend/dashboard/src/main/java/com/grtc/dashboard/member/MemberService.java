package com.grtc.dashboard.member;

import com.grtc.dashboard.global.common.PageResponse;
import com.grtc.dashboard.global.common.Pages;
import com.grtc.dashboard.global.common.SearchUtil;
import com.grtc.dashboard.global.exception.BusinessException;
import com.grtc.dashboard.global.exception.ErrorCode;
import com.grtc.dashboard.member.entity.MemberEntity;
import com.grtc.dashboard.member.entity.MemberStatus;
import com.grtc.dashboard.member.entity.Role;
import com.grtc.dashboard.member.repository.MemberRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// 회원관리(조회/검색/수정/상태·권한 변경) 비즈니스 로직
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    // 회원정보 화면의 선택 목록 (필요하면 여기만 수정하면 된다)
    public static final List<String> DEPARTMENTS = List.of("운영팀", "차량팀", "시설팀", "안전관리팀", "고객지원팀");
    public static final List<String> POSITIONS = List.of("시스템 관리자", "팀장", "대리", "사원");

    private final MemberRepository memberRepository;

    // 회원관리 목록: 이름/아이디/이메일 검색 + 권한/상태 필터 + 페이징 (화면과 같이 가입 순서대로)
    public PageResponse<MemberDto.ListItem> list(String keyword, Role role, MemberStatus status,
                                                 int page, int size) {
        Specification<MemberEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (SearchUtil.hasText(keyword)) {
                String like = SearchUtil.contains(keyword);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("name")), like, SearchUtil.ESCAPE),
                        cb.like(cb.lower(root.<String>get("loginId")), like, SearchUtil.ESCAPE),
                        cb.like(cb.lower(root.<String>get("email")), like, SearchUtil.ESCAPE)
                ));
            }
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<MemberEntity> result = memberRepository.findAll(spec, Pages.of(page, size, Sort.by("id")));

        // 번호는 검색 결과 기준 순번 (페이지가 넘어가도 이어서 증가)
        int base = result.getNumber() * result.getSize();
        List<MemberEntity> rows = result.getContent();
        List<MemberDto.ListItem> items = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            items.add(MemberDto.ListItem.of(base + i + 1, rows.get(i)));
        }
        return PageResponse.of(result, items);
    }

    // 회원 상세 조회
    public MemberDto.Detail get(Long id) {
        return MemberDto.Detail.from(find(id));
    }

    // 회원정보 수정
    @Transactional
    public MemberDto.Detail update(Long id, MemberDto.UpdateRequest request) {
        MemberEntity member = find(id);

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (memberRepository.existsByEmailAndIdNot(email, id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        member.updateProfile(
                request.name().trim(),
                email,
                blankToNull(request.phone()),
                blankToNull(request.department()),
                blankToNull(request.position())
        );
        log.info("[member] 회원정보 수정 id={}", id);
        return MemberDto.Detail.from(member);
    }

    // 회원 상태 변경 (정상 / 이용정지 / 탈퇴) - 본인 계정은 막는다(관리자가 스스로 잠기는 것 방지)
    @Transactional
    public MemberDto.Detail changeStatus(Long adminId, Long id, MemberStatus status) {
        if (adminId.equals(id)) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_SELF);
        }
        MemberEntity member = find(id);
        member.changeStatus(status);
        log.info("[member] 상태 변경 id={} status={}", id, status);
        return MemberDto.Detail.from(member);
    }

    // 회원 권한 변경 (관리자 / 일반회원) - 본인 계정은 막는다
    @Transactional
    public MemberDto.Detail changeRole(Long adminId, Long id, Role role) {
        if (adminId.equals(id)) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_SELF);
        }
        MemberEntity member = find(id);
        member.changeRole(role);
        log.info("[member] 권한 변경 id={} role={}", id, role);
        return MemberDto.Detail.from(member);
    }

    // 소속 부서 / 직급 선택 목록
    public MemberDto.Options options() {
        return new MemberDto.Options(DEPARTMENTS, POSITIONS);
    }

    private MemberEntity find(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // 빈 문자열은 null 로 저장 (이메일처럼 unique 컬럼에 "" 가 중복 저장되는 것도 방지)
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
