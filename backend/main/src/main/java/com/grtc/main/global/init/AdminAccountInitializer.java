package com.grtc.main.global.init;

import com.grtc.main.login.entity.AdminType;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 관리자 계정 자동 생성 (app.seed.enabled=true 이고, 아직 그 아이디가 없을 때 한 번만)
//  - 회원가입 화면으로는 일반회원(USER)만 만들어지므로, 관리자 계정은 여기서 만든다.
//  - 관리자 유형 네 가지마다 계정을 하나씩 만든다. 유형마다 볼 수 있는 페이지가 다르다. (AdminType 참고)
//      admin(시스템 관리자) / depthead(부서장) / cmanager(민원 담당자) / vmanager(차량 담당자)
//  - 아이디/비밀번호는 application.yaml 의 app.seed.admin-id / app.seed.admin-password 로 바꿀 수 있다.
//  - 로그인 화면에서 이 아이디로 로그인하면 redirectPath 가 /dashboard(관리자 대시보드)로 내려간다.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class AdminAccountInitializer implements ApplicationRunner {

    // 관리자 유형별 계정 아이디 (시스템 관리자는 app.seed.admin-id, 기본값 admin)
    static final String DEPARTMENT_HEAD_ID = "depthead";   // 부서장
    static final String COMPLAINT_MANAGER_ID = "cmanager"; // 민원 담당자
    static final String VEHICLE_MANAGER_ID = "vmanager";   // 차량 담당자

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-id:admin}")
    private String adminId;

    @Value("${app.seed.admin-password:admin1234!}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 시스템 관리자 (모든 페이지)
        if (!loginRepository.existsByLoginId(adminId)) {
            loginRepository.save(LoginEntity.builder()
                    .loginId(adminId)
                    .password(passwordEncoder.encode(adminPassword))
                    .name("관리자")
                    .email("admin@gjtc.or.kr")
                    .phone("010-1234-5678")
                    .department("운영팀")
                    .position("시스템 관리자")
                    .role(Role.ADMIN)
                    .adminType(AdminType.SYSTEM_ADMIN)
                    .build());
            log.info("[init] 관리자 계정 생성 loginId={} type={}", adminId, AdminType.SYSTEM_ADMIN);
        }

        // 나머지 관리자 유형별 계정 (비밀번호는 위 관리자 계정과 같다)
        createTypedAdmin(DEPARTMENT_HEAD_ID, AdminType.DEPARTMENT_HEAD);     // 대시보드
        createTypedAdmin(COMPLAINT_MANAGER_ID, AdminType.COMPLAINT_MANAGER); // 대시보드, 민원관리
        createTypedAdmin(VEHICLE_MANAGER_ID, AdminType.VEHICLE_MANAGER);     // 대시보드, 차량관리
    }

    private void createTypedAdmin(String loginId, AdminType type) {
        if (loginRepository.existsByLoginId(loginId)) {
            return;
        }
        loginRepository.save(LoginEntity.builder()
                .loginId(loginId)
                .password(passwordEncoder.encode(adminPassword))
                .name(type.getLabel())
                .role(Role.ADMIN)
                .adminType(type)
                .build());
        log.info("[init] 관리자 계정 생성 loginId={} type={}", loginId, type);
    }
}
