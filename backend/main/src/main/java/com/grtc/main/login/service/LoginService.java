package com.grtc.main.login.service;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.login.dto.LoginRequestDto;
import com.grtc.main.login.dto.LoginResponseDto;
import com.grtc.main.login.dto.SignUpRequestDto;
import com.grtc.main.login.dto.SignUpResponseDto;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.MemberStatus;
import com.grtc.main.login.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

// 회원가입, 로그인, 회원 조회 등 회원 관련 비즈니스 로직을 담당하는 서비스
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    // 회원 데이터를 조회/저장하는 리포지토리
    private final LoginRepository loginRepository;
    // 비밀번호 암호화 및 일치 여부 비교에 사용하는 인코더
    private final PasswordEncoder passwordEncoder;

    // 비밀번호 확인/아이디·이메일 중복을 검사한 뒤 비밀번호를 암호화해 회원을 저장하는 회원가입 처리
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto request) {
        // 아이디는 공백 제거 + 소문자로 정규화, 이름은 공백 제거
        String loginId = normalizeLoginId(request.getLoginId());
        request.setLoginId(loginId);
        request.setName(request.getName().trim());
        // 이메일도 공백 제거 + 소문자로 정규화 (내 정보 수정과 같은 규칙)
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        request.setEmail(email);

        // 회원가입 요청 로그 기록
        log.info("[signUp] 회원가입 요청 loginId={}", loginId);

        // 비밀번호와 비밀번호 확인이 다르면 예외 발생
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        // 아이디가 이미 존재하면 중복 아이디 예외 발생
        if (loginRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        // 이메일이 이미 존재하면 중복 이메일 예외 발생
        if (loginRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호를 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        try {
            // 엔티티로 변환해 DB에 저장(동시 가입으로 인한 중복은 여기서 바로 잡는다)
            LoginEntity saved = loginRepository.saveAndFlush(request.toEntity(encodedPassword));
            // 회원가입 완료 로그 기록
            log.info("[signUp] 회원가입 완료 id = {}", saved.getId());
            // 저장된 회원 정보를 응답 DTO로 변환해 반환
            return SignUpResponseDto.from(saved);
        } catch (DataIntegrityViolationException e) {
            // 위 검사를 통과한 직후 다른 요청이 같은 아이디/이메일로 먼저 가입한 경우 (어느 쪽인지는 여기서 알 수 없다)
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }
    }

    // 아이디 조회 후 비밀번호 일치 여부와 계정 상태를 검증해 로그인을 처리
    public LoginResponseDto login(LoginRequestDto request){
        // 입력 아이디의 공백 제거 및 소문자 정규화
        String loginId = normalizeLoginId(request.getLoginId());

        // 정규화한 아이디로 회원 조회(없으면 로그인 실패 예외)
        LoginEntity login = loginRepository.findByLoginId(loginId)
                .orElseThrow(()->{log.warn("[login] 실패 - 존재하지 않는 계정");
                return new BusinessException(ErrorCode.LOGIN_FAILED);
                });

        // 입력 비밀번호와 저장된 암호화 비밀번호를 비교, 불일치 시 로그인 실패 예외
        if(!passwordEncoder.matches(request.getPassword(), login.getPassword())){
            log.warn("[login] 실패 - 비밀번호 불일치 id={}", login.getId());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        // 비밀번호가 맞은 뒤에 상태를 확인(계정 존재 여부가 비밀번호 없이 노출되지 않도록)
        checkLoginable(login);

        // 로그인 성공 로그 기록
        log.info("[login] 성공 id={} role={}", login.getId(), login.getRole());

        // 조회한 회원 정보를 응답 DTO로 변환해 반환(관리자/사용자별 이동 경로 포함)
        return LoginResponseDto.from(login);
    }

    // 현재 로그인한 회원 정보 조회(사이드바 하단 표시, 새로고침 후 로그인 유지 확인용)
    public LoginResponseDto me(Long id) {
        return LoginResponseDto.from(getActiveMember(id));
    }

    // 회원 ID로 "이용 가능한" 회원 엔티티를 조회(없거나 정지/탈퇴면 예외 발생)
    // 세션은 계속 살아 있어도, 정지/탈퇴 처리된 회원은 서비스를 더 이상 쓰지 못하게 막는 용도
    public LoginEntity getActiveMember(Long id) {
        LoginEntity login = loginRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        checkLoginable(login);
        return login;
    }

    // 회원가입 화면의 아이디 중복 확인용
    public boolean isLoginIdAvailable(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return false;
        }
        return !loginRepository.existsByLoginId(normalizeLoginId(loginId));
    }

    // 정지/탈퇴 회원은 로그인과 서비스 이용을 막는다.
    private void checkLoginable(LoginEntity login) {
        if (login.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if (login.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.ACCOUNT_WITHDRAWN);
        }
    }

    // 아이디의 공백을 제거하고 소문자로 통일하는 내부 유틸 메서드
    private String normalizeLoginId(String loginId) {
        // 앞뒤 공백 제거 후 소문자로 변환
        return loginId.trim().toLowerCase(Locale.ROOT);
    }

}
