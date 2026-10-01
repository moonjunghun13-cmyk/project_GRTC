package com.grtc.dashboard.member.repository;

import com.grtc.dashboard.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 회원 데이터 조회/저장을 담당하는 JPA 리포지토리 (회원관리 검색용 Specification 지원)
@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long>, JpaSpecificationExecutor<MemberEntity> {

    // 아이디로 회원 한 명을 조회(로그인용)
    Optional<MemberEntity> findByLoginId(String loginId);
    // 해당 아이디의 회원 존재 여부를 확인(중복 검사용)
    boolean existsByLoginId(String loginId);
    // 해당 이메일의 회원 존재 여부를 확인(중복 검사용)
    boolean existsByEmail(String email);
    // 본인을 제외하고 같은 이메일을 쓰는 회원이 있는지 확인(회원정보 수정용)
    boolean existsByEmailAndIdNot(String email, Long id);
}
