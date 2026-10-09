package com.grtc.main.admin.dispatch;

import com.grtc.main.admin.vehicle.VehicleEntity;
import com.grtc.main.admin.vehicle.VehicleRepository;
import com.grtc.main.admin.vehicle.VehicleStatus;
import com.grtc.main.global.exception.BusinessException;
import com.grtc.main.global.exception.ErrorCode;
import com.grtc.main.login.dto.LoginResponseDto;
import com.grtc.main.login.entity.AdminType;
import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.login.service.TokenService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

// 배차 삭제 확인 (실제 DB 에 넣어 보고 테스트가 끝나면 전부 롤백한다)
//  - 다른 배차와 섞이지 않게 먼 미래 날짜(2099-03-01)와 테스트용 차량을 쓴다.
@SpringBootTest
@Transactional
class DispatchDeleteTest {

    private static final LocalDate DATE = LocalDate.of(2099, 3, 1);

    @Autowired
    private DispatchService dispatchService;
    @Autowired
    private DispatchRepository dispatchRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private LoginRepository loginRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private WebApplicationContext context;
    @PersistenceContext
    private EntityManager em;

    private VehicleEntity vehicle;

    @BeforeEach
    void setUp() {
        vehicle = vehicleRepository.save(VehicleEntity.builder()
                .vehicleNo("TST-" + System.nanoTime() % 1_000_000_000L).status(VehicleStatus.STANDBY).build());
    }

    // hour 시 정각 ~ 30분 배차를 하나 등록한다.
    private DispatchDto.Response create(int hour) {
        return dispatchService.create(new DispatchDto.Request(DATE, vehicle.getId(), "삭제테스트",
                LocalTime.of(hour, 0), LocalTime.of(hour, 30), null, null));
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private long countOnTestDate() {
        return dispatchService.list(null, DATE, vehicle.getId(), null, null, PageRequest.of(0, 10)).totalElements();
    }

    private static int sequenceOf(String dispatchNo) {
        return Integer.parseInt(dispatchNo.substring(dispatchNo.indexOf('-') + 1));
    }

    private void assertError(ThrowingCallable call, ErrorCode expected) {
        assertThatThrownBy(call).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 삭제하면_기록이_없어진다() {
        Long id = create(6).id();
        assertThat(countOnTestDate()).isEqualTo(1);

        dispatchService.delete(id);
        flushAndClear();

        assertThat(dispatchRepository.findById(id)).isEmpty();
        assertThat(countOnTestDate()).isZero();
        assertError(() -> dispatchService.get(id), ErrorCode.DISPATCH_NOT_FOUND);
    }

    @Test
    void 없는_배차를_삭제하면_없는_배차_오류가_난다() {
        Long id = create(6).id();
        dispatchService.delete(id);
        flushAndClear();

        assertError(() -> dispatchService.delete(id), ErrorCode.DISPATCH_NOT_FOUND);
    }

    @Test
    void 상태와_상관없이_삭제할_수_있다() {
        Long cancelled = create(6).id();
        dispatchService.cancel(cancelled);
        Long completed = create(7).id();
        dispatchService.update(completed,
                new DispatchDto.UpdateRequest(null, null, null, null, null, null, DispatchStatus.COMPLETED));
        flushAndClear();

        dispatchService.delete(cancelled);
        dispatchService.delete(completed);
        flushAndClear();

        assertThat(countOnTestDate()).isZero();
    }

    @Test
    void 삭제한_배차의_시간대에는_같은_차량을_다시_배차할_수_있다() {
        Long first = create(6).id();
        assertError(() -> create(6), ErrorCode.DISPATCH_TIME_CONFLICT);

        dispatchService.delete(first);
        flushAndClear();

        assertThat(create(6).id()).isNotNull();
    }

    // 예전 방식(그날 배차 수 + 1)이었다면 여기서 이미 있는 번호를 다시 만들어 등록이 실패한다.
    @Test
    void 중간_배차를_삭제한_뒤_등록해도_배차번호가_겹치지_않는다() {
        create(6);
        Long second = create(7).id();
        DispatchDto.Response third = create(8);

        dispatchService.delete(second);
        flushAndClear();

        DispatchDto.Response next = create(9);
        em.flush(); // DB 의 중복 금지 제약까지 통과하는지 확인

        assertThat(next.dispatchNo()).isNotEqualTo(third.dispatchNo());
        assertThat(sequenceOf(next.dispatchNo())).isEqualTo(sequenceOf(third.dispatchNo()) + 1);
    }

    // ---------- API 와 권한 (실제 보안 설정을 그대로 거친다) ----------

    private String tokenOf(AdminType type) {
        LoginEntity member = loginRepository.save(LoginEntity.builder()
                .loginId("dd" + System.nanoTime() % 1_000_000_000L).password("x").name("삭제권한테스트")
                .role(Role.ADMIN).adminType(type).build());
        return tokenService.createAccessToken(LoginResponseDto.from(member));
    }

    private int deleteStatus(Long id, String token) throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        var request = delete("/api/v1/admin/dispatches/" + id);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request).andReturn().getResponse().getStatus();
    }

    @Test
    void 시스템_관리자는_API로_배차를_삭제할_수_있다() throws Exception {
        Long id = create(6).id();
        flushAndClear();

        assertThat(deleteStatus(id, tokenOf(AdminType.SYSTEM_ADMIN))).isEqualTo(200);
        flushAndClear();
        assertThat(dispatchRepository.findById(id)).isEmpty();

        // 이미 지운 배차를 다시 지우면 404
        assertThat(deleteStatus(id, tokenOf(AdminType.SYSTEM_ADMIN))).isEqualTo(404);
    }

    // 배차관리는 시스템 관리자만 볼 수 있는 페이지다.
    @Test
    void 배차관리를_볼_수_없는_관리자와_비로그인은_삭제할_수_없다() throws Exception {
        Long id = create(6).id();
        flushAndClear();

        assertThat(deleteStatus(id, tokenOf(AdminType.VEHICLE_MANAGER))).isEqualTo(403);
        assertThat(deleteStatus(id, tokenOf(AdminType.COMPLAINT_MANAGER))).isEqualTo(403);
        assertThat(deleteStatus(id, tokenOf(AdminType.DEPARTMENT_HEAD))).isEqualTo(403);
        assertThat(deleteStatus(id, null)).isEqualTo(401);

        flushAndClear();
        assertThat(dispatchRepository.findById(id)).isPresent();
    }
}
