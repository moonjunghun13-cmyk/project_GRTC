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

// 관리자 계정 자동 생성 (app.seed.enabled=true 일 때, 서버가 켜질 때마다 확인해서 없는 계정만 만든다)
//  - 회원가입 화면으로는 일반회원(USER)만 만들어지므로, 관리자 계정은 여기서 만든다.
//  - 광주교통공사 임직원 세부사항 "3. 사용자별 부서/직급 분배" 의 8명을 그대로 만든다.
//      시스템 관리자 2명 : IT전략팀 - 차장 1명, 과장 1명
//      부서장 2명        : 고객사업처 1명, 차량운영처 1명 - 처장 2명
//      민원 담당자 2명   : 고객만족팀 - 대리 1명, 주임 1명
//      차량 담당자 2명   : 차량팀 - 부장 1명, 차장 1명
//  - 계정마다 볼 수 있는 페이지는 관리자 유형으로 정해진다. (AdminType: "4. 사용자별 열람 가능 페이지 범위")
//  - 비밀번호는 8개 모두 app.seed.admin-password (기본값 admin1234!) 이다.
//    첫 번째 시스템 관리자의 아이디는 app.seed.admin-id (기본값 admin) 로 바꿀 수 있다.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class AdminAccountInitializer implements ApplicationRunner {

    // 자동으로 만드는 관리자 계정 한 개 (email, phone 은 없으면 null)
    public record SeedAdmin(String loginId, String name, AdminType type, String department, String position,
                            String email, String phone) {
        public SeedAdmin(String loginId, String name, AdminType type, String department, String position) {
            this(loginId, name, type, department, position, null, null);
        }
    }

    // 예전 버전이 admin 계정에 넣던 부서/직급. 이 값이 그대로면 문서의 값으로 바꾼다. (직접 고친 값은 건드리지 않는다)
    static final String OLD_SEED_DEPARTMENT = "운영팀";
    static final String OLD_SEED_POSITION = "시스템 관리자";

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-id:admin}")
    private String adminId;

    @Value("${app.seed.admin-password:admin1234!}")
    private String adminPassword;

    // 문서 3번의 8명. (아이디 / 이름 / 관리자 유형 / 부서 / 직급)
    public List<SeedAdmin> seedAdmins() {
        return List.of(
                // 시스템 관리자: 모든 페이지
                new SeedAdmin(adminId, "관리자", AdminType.SYSTEM_ADMIN, "IT전략팀", "차장",
                        "admin@gjtc.or.kr", "010-1234-5678"),
                new SeedAdmin("admin2", "시스템 관리자 2", AdminType.SYSTEM_ADMIN, "IT전략팀", "과장"),
                // 부서장: 대시보드
                new SeedAdmin("depthead", "부서장", AdminType.DEPARTMENT_HEAD, "고객사업처", "처장"),
                new SeedAdmin("depthead2", "부서장 2", AdminType.DEPARTMENT_HEAD, "차량운영처", "처장"),
                // 민원 담당자: 대시보드, 민원관리
                new SeedAdmin("cmanager", "민원 담당자", AdminType.COMPLAINT_MANAGER, "고객만족팀", "대리"),
                new SeedAdmin("cmanager2", "민원 담당자 2", AdminType.COMPLAINT_MANAGER, "고객만족팀", "주임"),
                // 차량 담당자: 대시보드, 차량관리
                new SeedAdmin("vmanager", "차량 담당자", AdminType.VEHICLE_MANAGER, "차량팀", "부장"),
                new SeedAdmin("vmanager2", "차량 담당자 2", AdminType.VEHICLE_MANAGER, "차량팀", "차장")
        );
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmins().forEach(this::apply);
    }

    // 계정이 없으면 만들고, 이미 있으면 비어 있는 값만 채운다.
    //  - 이미 있는 계정의 비밀번호·이름·직접 고친 부서/직급은 건드리지 않는다.
    //  - 같은 아이디를 일반회원이 쓰고 있으면 아무것도 하지 않는다. (관리자로 바꾸지 않는다)
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
        // 예전 버전이 만든 계정 보완: 관리자 유형, 부서, 직급이 비어 있으면 채운다.
        if (member.getAdminType() == null) {
            member.changeAdminType(seed.type());
        }
        String department = needsSeedValue(member.getDepartment(), OLD_SEED_DEPARTMENT)
                ? seed.department() : member.getDepartment();
        String position = needsSeedValue(member.getPosition(), OLD_SEED_POSITION)
                ? seed.position() : member.getPosition();
        member.updateProfile(member.getName(), member.getEmail(), member.getPhone(), department, position);
    }

    // 값이 비어 있거나 예전 버전이 넣은 값 그대로일 때만 문서의 값으로 바꾼다.
    private static boolean needsSeedValue(String current, String oldSeedValue) {
        return current == null || current.isBlank() || current.equals(oldSeedValue);
    }
}
