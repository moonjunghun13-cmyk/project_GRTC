package com.grtc.main.qna.complain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ComplainRepository extends JpaRepository<ComplainEntity, Long>, JpaSpecificationExecutor<ComplainEntity> {

    // 민원번호 일련번호 계산용 (접수일 접두사 CM260930- 로 시작하는 민원 수)
    long countByComplainNoStartingWith(String prefix);
}
