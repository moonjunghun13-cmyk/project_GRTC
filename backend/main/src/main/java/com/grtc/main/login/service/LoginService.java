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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

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
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }
    }

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
        checkLoginable(login);

        // 로그인 성공 로그 기록
        log.info("[login] 성공 id={} role={}", login.getId(), login.getRole());

        // 조회한 회원 정보를 응답 DTO로 변환해 반환(관리자/사용자별 이동 경로 포함)
        return LoginResponseDto.from(login);
    }

    public LoginResponseDto me(Long id) {
        return LoginResponseDto.from(getActiveMember(id));
    }

    public LoginEntity getActiveMember(Long id) {
        LoginEntity login = loginRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        checkLoginable(login);
        return login;
    }

    public boolean isLoginIdAvailable(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return false;
        }
        return !loginRepository.existsByLoginId(normalizeLoginId(loginId));
    }

    private void checkLoginable(LoginEntity login) {
        if (login.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if (login.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.ACCOUNT_WITHDRAWN);
        }
    }

    private String normalizeLoginId(String loginId) {
        // 앞뒤 공백 제거 후 소문자로 변환
        return loginId.trim().toLowerCase(Locale.ROOT);
    }

}
