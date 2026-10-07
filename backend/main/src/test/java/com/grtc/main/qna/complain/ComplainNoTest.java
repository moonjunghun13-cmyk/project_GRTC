package com.grtc.main.qna.complain;

import com.grtc.main.admin.complain.AdminComplainDto;
import com.grtc.main.admin.complain.AdminComplainService;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.qna.complain.dto.ComplainDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// 민원번호 생성 확인: 그날 가장 큰 번호 + 1 이어야 한다. (민원 수 + 1 이 아니다)
//  - 실제 DB 에 넣어 보고 테스트가 끝나면 전부 롤백한다. (데이터가 남지 않는다)
@SpringBootTest
@Transactional
class ComplainNoTest {

    @Autowired
    private ComplainService complainService;
    @Autowired
    private AdminComplainService adminComplainService;
    @Autowired
    private ComplainRepository complainRepository;
    @Autowired
    private LoginRepository loginRepository;
    @PersistenceContext
    private EntityManager em;

    private LoginEntity user;
    private LoginEntity admin;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime() % 1_000_000_000L);
        user = loginRepository.save(LoginEntity.builder()
                .loginId("nou" + suffix).password("x").name("번호테스트").role(Role.USER).build());
        admin = loginRepository.save(LoginEntity.builder()
                .loginId("noa" + suffix).password("x").name("번호테스트").role(Role.ADMIN).build());
    }

    private ComplainDto.Detail create() {
        return complainService.create(user.getId(),
                new ComplainDto.SaveRequest(ComplainType.SIMPLE, null, "번호 테스트 제목", "번호 테스트 내용", null, null),
                null);
    }

    // CM261007-012 -> 12
    private static int sequenceOf(String complainNo) {
        return Integer.parseInt(complainNo.substring(complainNo.indexOf('-') + 1));
    }

    @Test
    void 번호는_하나씩_이어진다() {
        ComplainDto.Detail first = create();
        ComplainDto.Detail second = create();

        assertThat(first.complainNo()).matches("CM\\d{6}-\\d{3,}");
        assertThat(sequenceOf(second.complainNo())).isEqualTo(sequenceOf(first.complainNo()) + 1);
    }

    // 예전 방식(민원 수 + 1)에서는 여기서 이미 있는 번호를 다시 만들어 등록이 실패했다.
    @Test
    void 중간_번호가_지워져_있어도_새_번호가_겹치지_않는다() {
        ComplainDto.Detail first = create();
        ComplainDto.Detail second = create();
        ComplainDto.Detail third = create();

        // 예전에 실제로 삭제되어 번호가 비어 있는 상황을 만든다.
        complainRepository.deleteById(second.id());
        em.flush();
        em.clear();

        ComplainDto.Detail next = create();
        em.flush(); // DB 의 중복 금지 제약까지 통과하는지 확인

        assertThat(next.complainNo()).isNotIn(first.complainNo(), third.complainNo());
        assertThat(sequenceOf(next.complainNo())).isEqualTo(sequenceOf(third.complainNo()) + 1);
    }

    @Test
    void 관리자_등록도_같은_번호를_이어서_쓴다() {
        ComplainDto.Detail byUser = create();

        AdminComplainDto.Detail byAdmin = adminComplainService.create(admin.getId(),
                new AdminComplainDto.SaveRequest(ComplainType.SIMPLE, null, "번호 테스트 제목", "번호 테스트 내용", null, null),
                null);
        em.flush();

        assertThat(sequenceOf(byAdmin.complainNo())).isEqualTo(sequenceOf(byUser.complainNo()) + 1);
    }
}
