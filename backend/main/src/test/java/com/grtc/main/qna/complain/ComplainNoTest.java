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

    @Test
    void 중간_번호가_지워져_있어도_새_번호가_겹치지_않는다() {
        ComplainDto.Detail first = create();
        ComplainDto.Detail second = create();
        ComplainDto.Detail third = create();

        complainRepository.deleteById(second.id());
        em.flush();
        em.clear();

        ComplainDto.Detail next = create();
        em.flush();

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
