package com.grtc.main.admin.dispatch;

import com.grtc.main.admin.vehicle.VehicleEntity;
import com.grtc.main.admin.vehicle.VehicleRepository;
import com.grtc.main.admin.vehicle.VehicleStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;

import static org.assertj.core.api.Assertions.assertThat;

// 트랜잭션 롤백 없이 실제로 커밋되는 테스트라 만든 데이터는 끝나고 지운다
@SpringBootTest
class DispatchNoConcurrencyTest {

    @Autowired
    DispatchService dispatchService;
    @Autowired
    DispatchRepository dispatchRepository;
    @Autowired
    VehicleRepository vehicleRepository;

    private VehicleEntity vehicle;
    private final Queue<DispatchDto.Response> created = new ConcurrentLinkedQueue<>();

    @BeforeEach
    void saveVehicle() {
        vehicle = vehicleRepository.save(VehicleEntity.builder()
                .vehicleNo("TST-" + System.nanoTime() % 1_000_000_000L)
                .status(VehicleStatus.STANDBY)
                .build());
    }

    @AfterEach
    void cleanUp() {
        dispatchRepository.deleteAllById(created.stream().map(DispatchDto.Response::id).toList());
        vehicleRepository.deleteById(vehicle.getId());
    }

    @Test
    void 동시에_등록해도_배차번호가_겹치지_않는다() throws InterruptedException {
        int threads = 8;
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Queue<Throwable> errors = new ConcurrentLinkedQueue<>();
        List<Thread> workers = new ArrayList<>();

        for (int h = 1; h <= threads; h++) {
            LocalTime from = LocalTime.of(h, 0);
            Thread t = new Thread(() -> {
                try {
                    barrier.await();
                    created.add(dispatchService.create(new DispatchDto.Request(
                            LocalDate.of(2099, 12, 31), vehicle.getId(), "동시테스트",
                            from, from.plusMinutes(30), null, null)));
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
            workers.add(t);
            t.start();
        }
        for (Thread t : workers) {
            t.join(30_000);
        }

        assertThat(errors).isEmpty();
        assertThat(created).extracting(DispatchDto.Response::dispatchNo)
                .hasSize(threads)
                .doesNotHaveDuplicates();
    }
}
