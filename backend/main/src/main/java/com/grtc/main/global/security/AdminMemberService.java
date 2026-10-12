package com.grtc.main.global.security;

import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.MemberStatus;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final LoginRepository memberRepository;

    public LoginEntity getActiveAdmin(Long id) {
        LoginEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.ACCOUNT_WITHDRAWN);
        }
        if (member.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.ADMIN_ONLY);
        }
        return member;
    }

    public LoginEntity getActiveAdminFor(Long id, String requestUri) {
        LoginEntity member = getActiveAdmin(id);
        if (!member.resolveAdminType().canCallApi(requestUri)) {
            throw new BusinessException(ErrorCode.ADMIN_PAGE_FORBIDDEN);
        }
        return member;
    }
}
