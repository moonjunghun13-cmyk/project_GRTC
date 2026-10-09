package com.grtc.main.admin.dispatch;

import com.grtc.main.admin.vehicle.VehicleEntity;
import com.grtc.main.admin.vehicle.VehicleRepository;
import com.grtc.main.admin.vehicle.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// 여러 사람이 같은 순간에 같은 날짜의 배차를 등록해도 배차번호가 겹치지 않는지 확인
//  - 요청마다 따로 커밋되어야 하므로 이 테스트는 롤백 방식이 아니다.
//    대신 테스트가 만든 배차와 차량은 끝날 때 직접 지운다. (성공/실패와 상관없이)
@SpringBootTest
class DispatchNoConcurrencyTest {

    private static final int REQUESTS = 8;
    private static final LocalDate DATE = LocalDate.of(2099, 12, 31);

    @Autowired
    private DispatchService dispatchService;
    @Autowired
    private DispatchRepository dispatchRepository;
    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void 동시에_등록해도_배차번호가_겹치지_않는다() throws Exception {
        VehicleEntity vehicle = vehicleRepository.save(VehicleEntity.builder()
                .vehicleNo("TST-" + System.nanoTime() % 1_000_000_000L).status(VehicleStatus.STANDBY).build());
        List<Long> createdIds = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(REQUESTS);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < REQUESTS; i++) {
                int hour = i + 1; // 시간이 겹치지 않게 요청마다 다른 시각
                futures.add(pool.submit(() -> {
                    start.await(); // 모두 준비된 뒤 한꺼번에 출발
                    DispatchDto.Response created = dispatchService.create(new DispatchDto.Request(
                            DATE, vehicle.getId(), "동시테스트", LocalTime.of(hour, 0), LocalTime.of(hour, 30),
                            null, null));
                    createdIds.add(created.id());
                    return created.dispatchNo();
                }));
            }
            start.countDown();

            List<String> numbers = new ArrayList<>();
            for (Future<String> future : futures) {
                numbers.add(future.get(30, TimeUnit.SECONDS)); // 하나라도 등록에 실패하면 여기서 테스트 실패
            }
            assertThat(numbers).hasSize(REQUESTS).doesNotHaveDuplicates();
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(10, TimeUnit.SECONDS);
            dispatchRepository.deleteAllById(createdIds);
            vehicleRepository.deleteById(vehicle.getId());
        }
    }
}
