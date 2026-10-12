package com.grtc.main.login.repository;

import com.grtc.main.login.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<LoginEntity, Long>, JpaSpecificationExecutor<LoginEntity> {

    Optional<LoginEntity> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}
