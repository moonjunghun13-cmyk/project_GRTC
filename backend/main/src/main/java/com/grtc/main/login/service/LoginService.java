package com.grtc.main.login.service;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.login.dto.LoginRequestDto;
import com.grtc.main.login.dto.SignUpRequestDto;
import com.grtc.main.login.dto.SignUpResponseDto;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto request) {
        String nickname = request.getNickname().trim();
        String email = request.getEmail().trim();
        request.setEmail(email);
        request.setNickname(nickname);

        log.info("[signUp] 회원가입 요청 nickname={}", nickname);

        if (loginRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (loginRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        LoginEntity saved = loginRepository.save(request.toEntity(encodedPassword));
        log.info("[singUP] 회원가입 완료 id = {}", saved.getId());
        return SignUpResponseDto.from(saved);
    }
    public SignUpResponseDto login(LoginRequestDto request){
        String nickname = request.getNickname().trim();

        LoginEntity login = loginRepository.findByNickname(nickname)
                .orElseThrow(()->{log.warn("존재하지 않는 계정 nickname={}", nickname);
                return new BusinessException(ErrorCode.LOGIN_FAILED);
                });

        if(!passwordEncoder.matches(request.getPassword(), login.getPassword())){
            log.warn("[login]실패 - 비밀번호 불일지 nickname ={}", nickname);
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        log.info("[login]성공 id={}", login.getId());

        return SignUpResponseDto.from(login);
    }

}
