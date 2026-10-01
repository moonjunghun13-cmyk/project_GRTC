package com.grtc.main.member;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

// 내 정보 조회/수정 비즈니스 로직 (회원 본인용)
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    // 회원정보 화면의 선택 목록 (dashboard 서버의 MemberService 와 같은 값으로 맞춘다)
    public static final List<String> DEPARTMENTS = List.of("운영팀", "차량팀", "시설팀", "안전관리팀", "고객지원팀");
    public static final List<String> POSITIONS = List.of("시스템 관리자", "팀장", "대리", "사원");

    private final LoginRepository loginRepository;
    private final LoginService loginService;

    // 내 정보 조회 (정지/탈퇴 회원은 막는다)
    public MemberDto.Detail get(Long id) {
        return MemberDto.Detail.from(loginService.getActiveMember(id));
    }

    // 내 정보 수정
    @Transactional
    public MemberDto.Detail update(Long id, MemberDto.UpdateRequest request) {
        LoginEntity member = loginService.getActiveMember(id);

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (loginRepository.existsByEmailAndIdNot(email, id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        member.updateProfile(
                request.name().trim(),
                email,
                blankToNull(request.phone()),
                blankToNull(request.department()),
                blankToNull(request.position())
        );
        log.info("[member] 내 정보 수정 id={}", id);
        return MemberDto.Detail.from(member);
    }

    // 소속 부서 / 직급 선택 목록
    public MemberDto.Options options() {
        return new MemberDto.Options(DEPARTMENTS, POSITIONS);
    }

    // 빈 문자열은 null 로 저장 (이메일처럼 unique 컬럼에 "" 가 중복 저장되는 것도 방지)
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
