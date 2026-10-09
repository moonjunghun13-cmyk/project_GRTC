package com.grtc.main.global.init;

import com.grtc.main.global.init.AdminAccountInitializer.SeedAdmin;
import com.grtc.main.login.dto.LoginResponseDto;
import com.grtc.main.login.entity.AdminPage;
import com.grtc.main.login.entity.AdminType;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.member.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 관리자 계정 자동 생성 확인 (광주교통공사 임직원 세부사항 3번의 8명, 4번의 열람 가능 페이지)
//  - 계정을 만들고 고치는 동작은 임시 아이디로 확인하고, 테스트가 끝나면 전부 롤백한다.
@SpringBootTest
@Transactional
class AdminAccountInitializerTest {

    @Autowired
    private AdminAccountInitializer initializer;
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @PersistenceContext
    private EntityManager em;

    @Value("${app.seed.admin-password:admin1234!}")
    private String seedPassword;

    private static String tempId() {
        return "sd" + System.nanoTime() % 1_000_000_000L;
    }

    private LoginEntity reload(String loginId) {
        em.flush();
        em.clear();
        return loginRepository.findByLoginId(loginId).orElseThrow();
    }

    private static List<String> pageLabels(AdminType type) {
        return type.pageList().stream().map(AdminPage::getLabel).toList();
    }

    // ---------- 문서와 같은지 ----------

    @Test
    void 문서_3번의_8명이_부서와_직급까지_그대로_정의되어_있다() {
        List<SeedAdmin> seeds = initializer.seedAdmins();

        assertThat(seeds).extracting(SeedAdmin::loginId).doesNotHaveDuplicates();
        assertThat(seeds).extracting(s -> s.type().getLabel() + " / " + s.department() + " / " + s.position())
                .containsExactly(
                        "시스템 관리자 / IT전략팀 / 차장",
                        "시스템 관리자 / IT전략팀 / 과장",
                        "부서장 / 고객사업처 / 처장",
                        "부서장 / 차량운영처 / 처장",
                        "민원 담당자 / 고객만족팀 / 대리",
                        "민원 담당자 / 고객만족팀 / 주임",
                        "차량 담당자 / 차량팀 / 부장",
                        "차량 담당자 / 차량팀 / 차장");

        // 회원정보 화면의 부서/직급 선택 목록에 있는 값이어야 한다. (없으면 수정 화면에서 선택이 비어 보인다)
        assertThat(seeds).allSatisfy(s -> {
            assertThat(MemberService.DEPARTMENTS).contains(s.department());
            assertThat(MemberService.POSITIONS).contains(s.position());
        });
    }

    @Test
    void 문서_4번대로_계정마다_열람_가능_페이지가_정해진다() {
        for (SeedAdmin seed : initializer.seedAdmins()) {
            List<String> pages = pageLabels(seed.type());
            switch (seed.type()) {
                case SYSTEM_ADMIN -> assertThat(pages).as(seed.loginId())
                        .containsExactly("대시보드", "차량관리", "배차관리", "운행관리", "민원관리", "회원관리");
                case DEPARTMENT_HEAD -> assertThat(pages).as(seed.loginId()).containsExactly("대시보드");
                case COMPLAINT_MANAGER -> assertThat(pages).as(seed.loginId()).containsExactly("대시보드", "민원관리");
                case VEHICLE_MANAGER -> assertThat(pages).as(seed.loginId()).containsExactly("대시보드", "차량관리");
            }
        }
        // 유형마다 2명씩
        for (AdminType type : AdminType.values()) {
            assertThat(initializer.seedAdmins()).filteredOn(s -> s.type() == type).as(type.getLabel()).hasSize(2);
        }
    }

    // ---------- 계정을 만들고 고치는 동작 ----------

    @Test
    void 없는_계정은_유형_부서_직급을_넣어_만든다() {
        SeedAdmin seed = new SeedAdmin(tempId(), "시드테스트", AdminType.COMPLAINT_MANAGER, "고객만족팀", "주임");

        initializer.apply(seed);
        initializer.apply(seed); // 다시 실행해도 또 만들지 않는다

        LoginEntity member = reload(seed.loginId());
        assertThat(member.getRole()).isEqualTo(Role.ADMIN);
        assertThat(member.getAdminType()).isEqualTo(AdminType.COMPLAINT_MANAGER);
        assertThat(member.getDepartment()).isEqualTo("고객만족팀");
        assertThat(member.getPosition()).isEqualTo("주임");
        assertThat(passwordEncoder.matches(seedPassword, member.getPassword())).isTrue();

        // 로그인 응답에 내려가는 열람 가능 페이지
        assertThat(LoginResponseDto.from(member).getPages()).extracting(LoginResponseDto.PageInfo::code)
                .containsExactly("DASHBOARD", "COMPLAINTS");
    }

    @Test
    void 이미_있는_계정은_비어_있는_값만_채운다() {
        String loginId = tempId();
        loginRepository.save(LoginEntity.builder().loginId(loginId).password("그대로").name("원래이름")
                .role(Role.ADMIN).position("직접고친직급").build());

        initializer.apply(new SeedAdmin(loginId, "시드이름", AdminType.VEHICLE_MANAGER, "차량팀", "부장"));

        LoginEntity member = reload(loginId);
        assertThat(member.getAdminType()).isEqualTo(AdminType.VEHICLE_MANAGER); // 비어 있던 유형은 채운다
        assertThat(member.getDepartment()).isEqualTo("차량팀");                  // 비어 있던 부서도 채운다
        assertThat(member.getPosition()).isEqualTo("직접고친직급");              // 직접 고친 값은 그대로
        assertThat(member.getName()).isEqualTo("원래이름");
        assertThat(member.getPassword()).isEqualTo("그대로");
    }

    // 예전 버전은 admin 계정을 운영팀 / 시스템 관리자로 만들었다. 그 값 그대로면 문서의 값으로 바꾼다.
    @Test
    void 예전_버전이_넣은_부서와_직급은_문서의_값으로_바뀐다() {
        String loginId = tempId();
        loginRepository.save(LoginEntity.builder().loginId(loginId).password("x").name("관리자").role(Role.ADMIN)
                .department(AdminAccountInitializer.OLD_SEED_DEPARTMENT)
                .position(AdminAccountInitializer.OLD_SEED_POSITION).build());

        initializer.apply(new SeedAdmin(loginId, "관리자", AdminType.SYSTEM_ADMIN, "IT전략팀", "차장"));

        LoginEntity member = reload(loginId);
        assertThat(member.getAdminType()).isEqualTo(AdminType.SYSTEM_ADMIN);
        assertThat(member.getDepartment()).isEqualTo("IT전략팀");
        assertThat(member.getPosition()).isEqualTo("차장");
    }

    @Test
    void 이미_지정된_관리자_유형은_바꾸지_않는다() {
        String loginId = tempId();
        loginRepository.save(LoginEntity.builder().loginId(loginId).password("x").name("관리자").role(Role.ADMIN)
                .adminType(AdminType.DEPARTMENT_HEAD).build());

        initializer.apply(new SeedAdmin(loginId, "관리자", AdminType.SYSTEM_ADMIN, "IT전략팀", "차장"));

        assertThat(reload(loginId).getAdminType()).isEqualTo(AdminType.DEPARTMENT_HEAD);
    }

    @Test
    void 같은_아이디를_일반회원이_쓰고_있으면_건드리지_않는다() {
        String loginId = tempId();
        loginRepository.save(LoginEntity.builder().loginId(loginId).password("x").name("일반회원").role(Role.USER).build());

        initializer.apply(new SeedAdmin(loginId, "관리자", AdminType.SYSTEM_ADMIN, "IT전략팀", "차장"));

        LoginEntity member = reload(loginId);
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getAdminType()).isNull();
        assertThat(member.getDepartment()).isNull();
    }
}
