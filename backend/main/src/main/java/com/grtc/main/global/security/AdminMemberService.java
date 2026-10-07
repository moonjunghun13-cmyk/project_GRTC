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

// 세션의 회원 ID 로 "지금도 이용 가능한 관리자"인지 DB 기준으로 확인한다.
//  - 로그인 후에 권한이 일반회원으로 바뀌었거나, 정지/탈퇴 처리된 경우를 막기 위한 용도
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

    // 위 확인에 더해, 이 관리자의 유형으로 해당 관리자 API(= 그 페이지)를 쓸 수 있는지도 확인한다.
    //  - 예) 민원 담당자가 차량관리 API 를 호출하면 403
    public LoginEntity getActiveAdminFor(Long id, String requestUri) {
        LoginEntity member = getActiveAdmin(id);
        if (!member.resolveAdminType().canCallApi(requestUri)) {
            throw new BusinessException(ErrorCode.ADMIN_PAGE_FORBIDDEN);
        }
        return member;
    }
}
