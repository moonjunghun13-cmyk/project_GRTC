package com.grtc.main.member;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.login.service.LoginService;
import com.grtc.main.login.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final TokenService tokenService;
    private final ProfileImageService profileImageService;
    private final PasswordEncoder passwordEncoder;

    // 내 정보 조회 (정지/탈퇴 회원은 막는다)
    public MemberDto.Detail get(Long id) {
        return MemberDto.Detail.from(loginService.getActiveMember(id));
    }

    // 내 정보 수정 (PATCH: 요청에 들어 있는 항목만 바꾼다)
    @Transactional
    public MemberDto.Detail update(Long id, MemberDto.UpdateRequest request) {
        LoginEntity member = loginService.getActiveMember(id);

        String name = request.name() != null ? request.name().trim() : member.getName();

        String email = member.getEmail();
        if (request.email() != null) {
            email = request.email().trim().toLowerCase(Locale.ROOT);
            if (loginRepository.existsByEmailAndIdNot(email, id)) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }
        }

        member.updateProfile(
                name,
                email,
                request.phone() != null ? blankToNull(request.phone()) : member.getPhone(),
                request.department() != null ? blankToNull(request.department()) : member.getDepartment(),
                request.position() != null ? blankToNull(request.position()) : member.getPosition()
        );
        log.info("[member] 내 정보 수정 id={}", id);
        return MemberDto.Detail.from(member);
    }

    // 프로필 이미지 변경: 새 이미지와 썸네일을 저장하고, 예전 파일은 지운다.
    @Transactional
    public MemberDto.ProfileImage changeProfileImage(Long id, MultipartFile file) {
        LoginEntity member = loginService.getActiveMember(id);
        String oldImage = member.getProfileImage();
        String oldThumbnail = member.getProfileThumbnail();

        ProfileImageService.Stored stored = profileImageService.store(file);
        member.changeProfileImage(stored.imageName(), stored.thumbnailName());
        profileImageService.delete(oldImage, oldThumbnail);

        log.info("[member] 프로필 이미지 변경 id={}", id);
        return new MemberDto.ProfileImage(
                ProfileImageService.urlOf(stored.imageName()),
                ProfileImageService.urlOf(stored.thumbnailName()));
    }

    // 비밀번호 변경: 현재 비밀번호를 확인한 뒤 새 비밀번호로 바꾼다.
    //  - 바꾼 뒤에는 발급돼 있던 Refresh 토큰을 모두 지운다. (다른 기기 포함, 다시 로그인해야 한다)
    @Transactional
    public void changePassword(Long id, MemberDto.PasswordRequest request) {
        LoginEntity member = loginService.getActiveMember(id);

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_MISMATCH);
        }
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        member.changePassword(passwordEncoder.encode(request.newPassword()));
        tokenService.revokeAll(id);
        log.info("[member] 비밀번호 변경 id={}", id);
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
