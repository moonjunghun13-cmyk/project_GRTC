package com.grtc.main.qna.complain;

import com.grtc.main.admin.complain.AdminComplainDto;
import com.grtc.main.admin.complain.AdminComplainService;
import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.qna.complain.dto.ComplainDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 민원 삭제 = 철회 처리 확인
//  - 민원인에게는 삭제된 것처럼 안 보이고, 관리자에게는 '철회' 상태로 남아야 한다.
//  - 실제 DB 에 넣어 보고(제약 조건 확인) 테스트가 끝나면 전부 롤백한다. (데이터가 남지 않는다)
@SpringBootTest
@Transactional
class ComplainWithdrawTest {

    private static final String TITLE = "철회 테스트 제목";
    private static final String CONTENT = "철회 테스트 내용";

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
    private LoginEntity other;
    private LoginEntity admin;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime() % 1_000_000_000L);
        user = loginRepository.save(member("wdu" + suffix, Role.USER));
        other = loginRepository.save(member("wdo" + suffix, Role.USER));
        admin = loginRepository.save(member("wda" + suffix, Role.ADMIN));
    }

    private LoginEntity member(String loginId, Role role) {
        return LoginEntity.builder().loginId(loginId).password("x").name("철회테스트").role(role).build();
    }

    private ComplainDto.SaveRequest saveRequest(String title, String content) {
        return new ComplainDto.SaveRequest(ComplainType.SIMPLE, null, title, content, null, null);
    }

    private Long createComplain() {
        return complainService.create(user.getId(), saveRequest(TITLE, CONTENT), null).id();
    }

    // 삭제한 뒤 DB 에 반영(flush)하고 캐시를 비워서, 이후 조회가 실제 DB 값을 읽게 한다.
    private Long createAndWithdraw() {
        Long id = createComplain();
        complainService.delete(user.getId(), id);
        em.flush();
        em.clear();
        return id;
    }

    private ComplainDto.SearchCondition byTitle() {
        return new ComplainDto.SearchCondition(TITLE, null, null, null);
    }

    private void assertError(ThrowingCallable call, ErrorCode expected) {
        assertThatThrownBy(call).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 삭제하면_줄은_남고_상태만_철회로_바뀐다() {
        Long id = createAndWithdraw();

        ComplainEntity saved = complainRepository.findById(id).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(ComplainStatus.WITHDRAWN);
        assertThat(saved.getWithdrawnAt()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo(TITLE);
    }

    @Test
    void 철회_전에는_목록에_정상적으로_나온다() {
        Long id = createComplain();
        em.flush();
        em.clear();

        assertThat(complainService.listForUser(user.getId(), byTitle(), 1, 50).content())
                .anyMatch(item -> item.id().equals(id));
    }

    @Test
    void 민원인에게는_삭제된_민원처럼_보이지_않는다() {
        Long id = createAndWithdraw();

        // 목록: 본인에게도, 다른 회원에게도 안 나온다
        assertThat(complainService.listForUser(user.getId(), byTitle(), 1, 50).content())
                .noneMatch(item -> item.id().equals(id));
        assertThat(complainService.listForUser(other.getId(), byTitle(), 1, 50).content())
                .noneMatch(item -> item.id().equals(id));

        // 상세 / 수정 / 다시 삭제: 모두 '없는 민원'
        assertError(() -> complainService.get(user.getId(), id), ErrorCode.COMPLAIN_NOT_FOUND);
        assertError(() -> complainService.update(user.getId(), id, saveRequest("다시", "다시"), null),
                ErrorCode.COMPLAIN_NOT_FOUND);
        assertError(() -> complainService.delete(user.getId(), id), ErrorCode.COMPLAIN_NOT_FOUND);

        // 사용자 화면의 처리상태 선택 목록에도 '철회'는 없다
        assertThat(complainService.options().statuses())
                .noneMatch(s -> s.code().equals(ComplainStatus.WITHDRAWN.name()));
    }

    @Test
    void 관리자에게는_철회_상태로_그대로_보인다() {
        Long id = createAndWithdraw();

        AdminComplainDto.Detail detail = adminComplainService.get(admin.getId(), id);
        assertThat(detail.status()).isEqualTo(ComplainStatus.WITHDRAWN);
        assertThat(detail.statusLabel()).isEqualTo("철회");
        assertThat(detail.withdrawnAt()).isNotNull();
        assertThat(detail.content()).isEqualTo(CONTENT);

        // 처리상태 필터 status=WITHDRAWN 으로 모아 볼 수 있다
        var withdrawnOnly = adminComplainService.list(admin.getId(),
                new AdminComplainDto.SearchCondition(TITLE, null, null, ComplainStatus.WITHDRAWN), 1, 50);
        assertThat(withdrawnOnly.content()).anyMatch(item -> item.id().equals(id) && item.withdrawnAt() != null);

        // 관리자 화면의 처리상태 선택 목록에는 '철회'가 있다
        assertThat(adminComplainService.options().statuses())
                .anyMatch(s -> s.code().equals(ComplainStatus.WITHDRAWN.name()));
    }

    @Test
    void 철회된_민원에는_답변할_수_없다() {
        Long id = createAndWithdraw();

        assertError(() -> adminComplainService.answer(admin.getId(), id,
                new AdminComplainDto.AnswerRequest(ComplainStatus.ANSWERED, "답변")), ErrorCode.COMPLAIN_WITHDRAWN);
    }

    @Test
    void 관리자가_답변으로_철회_상태를_만들_수는_없다() {
        Long id = createComplain();

        assertError(() -> adminComplainService.answer(admin.getId(), id,
                new AdminComplainDto.AnswerRequest(ComplainStatus.WITHDRAWN, "답변")), ErrorCode.INVALID_ANSWER_STATUS);
    }

    @Test
    void 남의_민원은_삭제할_수_없다() {
        Long id = createComplain();

        // 일반 사용자에게 남의 민원은 '없는 민원'이다
        assertError(() -> complainService.delete(other.getId(), id), ErrorCode.COMPLAIN_NOT_FOUND);
        assertThat(complainRepository.findById(id).orElseThrow().getStatus()).isEqualTo(ComplainStatus.WAITING);
    }
}
