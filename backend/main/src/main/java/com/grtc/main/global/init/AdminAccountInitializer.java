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

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class AdminAccountInitializer implements ApplicationRunner {

    public record SeedAdmin(String loginId, String name, AdminType type, String department, String position,
                            String email, String phone) {
        public SeedAdmin(String loginId, String name, AdminType type, String department, String position) {
            this(loginId, name, type, department, position, null, null);
        }
    }

    static final String OLD_SEED_DEPARTMENT = "운영팀";
    static final String OLD_SEED_POSITION = "시스템 관리자";

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-id:admin}")
    private String adminId;

    @Value("${app.seed.admin-password:admin1234!}")
    private String adminPassword;

    public List<SeedAdmin> seedAdmins() {
        return List.of(
                new SeedAdmin(adminId, "관리자", AdminType.SYSTEM_ADMIN, "IT전략팀", "차장",
                        "admin@gjtc.or.kr", "010-1234-5678"),
                new SeedAdmin("admin2", "시스템 관리자 2", AdminType.SYSTEM_ADMIN, "IT전략팀", "과장"),
                new SeedAdmin("cmanager", "민원 담당자", AdminType.COMPLAINT_MANAGER, "고객만족팀", "대리"),
                new SeedAdmin("cmanager2", "민원 담당자 2", AdminType.COMPLAINT_MANAGER, "고객만족팀", "주임"),
                new SeedAdmin("vmanager", "차량 담당자", AdminType.VEHICLE_MANAGER, "차량팀", "부장"),
                new SeedAdmin("vmanager2", "차량 담당자 2", AdminType.VEHICLE_MANAGER, "차량팀", "차장")
        );
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmins().forEach(this::apply);
    }

    @Transactional
    public void apply(SeedAdmin seed) {
        Optional<LoginEntity> existing = loginRepository.findByLoginId(seed.loginId());
        if (existing.isEmpty()) {
            loginRepository.save(LoginEntity.builder()
                    .loginId(seed.loginId())
                    .password(passwordEncoder.encode(adminPassword))
                    .name(seed.name())
                    .email(seed.email())
                    .phone(seed.phone())
                    .department(seed.department())
                    .position(seed.position())
                    .role(Role.ADMIN)
                    .adminType(seed.type())
                    .build());
            log.info("[init] 관리자 계정 생성 loginId={} type={} {} {}",
                    seed.loginId(), seed.type(), seed.department(), seed.position());
            return;
        }

        LoginEntity member = existing.get();
        if (member.getRole() != Role.ADMIN) {
            return;
        }
        if (member.getAdminType() == null) {
            member.changeAdminType(seed.type());
        }
        String department = needsSeedValue(member.getDepartment(), OLD_SEED_DEPARTMENT)
                ? seed.department() : member.getDepartment();
        String position = needsSeedValue(member.getPosition(), OLD_SEED_POSITION)
                ? seed.position() : member.getPosition();
        member.updateProfile(member.getName(), member.getEmail(), member.getPhone(), department, position);
    }

    private static boolean needsSeedValue(String current, String oldSeedValue) {
        return current == null || current.isBlank() || current.equals(oldSeedValue);
    }
}
