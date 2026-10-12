package com.grtc.main.qna.complain;

import com.grtc.main.admin.complain.AdminComplainDto;
import com.grtc.main.admin.complain.AdminComplainService;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.qna.complain.dto.ComplainDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ComplainListOrderTest {

    private static final String TITLE = "정렬 테스트 제목";

    @Autowired
    private ComplainService complainService;
    @Autowired
    private AdminComplainService adminComplainService;
    @Autowired
    private LoginRepository loginRepository;
    @PersistenceContext
    private EntityManager em;

    @Test
    void 최신_민원이_목록_맨_위에_나온다() {
        String suffix = String.valueOf(System.nanoTime() % 1_000_000_000L);
        LoginEntity user = loginRepository.save(LoginEntity.builder()
                .loginId("odu" + suffix).password("x").name("정렬테스트").role(Role.USER).build());

        Long first = create(user, TITLE + " 1");
        Long second = create(user, TITLE + " 2");
        Long third = create(user, TITLE + " 3");
        em.flush();
        em.clear();

        List<Long> userOrder = complainService
                .listForUser(user.getId(), new ComplainDto.SearchCondition(TITLE, null, null, null), 1, 10)
                .content().stream().map(ComplainDto.ListItem::id).toList();
        assertThat(userOrder).containsExactly(third, second, first);

        List<Long> adminOrder = adminComplainService
                .list(user.getId(), new AdminComplainDto.SearchCondition(TITLE, null, null, null), 1, 10)
                .content().stream().map(AdminComplainDto.ListItem::id).toList();
        assertThat(adminOrder).containsExactly(third, second, first);
    }

    private Long create(LoginEntity user, String title) {
        return complainService.create(user.getId(),
                new ComplainDto.SaveRequest(ComplainType.SIMPLE, null, title, "정렬 테스트 내용", null, null),
                null).id();
    }
}
