package com.grtc.dashboard.global.security;

import com.grtc.dashboard.global.exception.BusinessException;
import com.grtc.dashboard.global.exception.ErrorCode;
import com.grtc.dashboard.member.entity.MemberEntity;
import com.grtc.dashboard.member.entity.MemberStatus;
import com.grtc.dashboard.member.entity.Role;
import com.grtc.dashboard.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 세션의 회원 ID 로 "지금도 이용 가능한 관리자"인지 DB 기준으로 확인한다.
//  - 로그인 후에 권한이 일반회원으로 바뀌었거나, 정지/탈퇴 처리된 경우를 막기 위한 용도
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public MemberEntity getActiveAdmin(Long id) {
        MemberEntity member = memberRepository.findById(id)
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
}
