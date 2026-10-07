package com.grtc.main.qna.complain;

import com.grtc.main.login.entity.LoginEntity;
import com.grtc.main.login.entity.Role;
import com.grtc.main.login.repository.LoginRepository;
import com.grtc.main.qna.complain.dto.ComplainDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// 여러 사람이 같은 순간에 민원을 등록해도 민원번호가 겹치지 않는지 확인
//  - 요청마다 따로 커밋되어야 하므로 이 테스트는 롤백 방식이 아니다.
//    대신 테스트가 만든 민원과 회원은 끝날 때 직접 지운다. (성공/실패와 상관없이)
@SpringBootTest
class ComplainNoConcurrencyTest {

    private static final int REQUESTS = 8;

    @Autowired
    private ComplainService complainService;
    @Autowired
    private ComplainRepository complainRepository;
    @Autowired
    private LoginRepository loginRepository;

    @Test
    void 동시에_등록해도_민원번호가_겹치지_않는다() throws Exception {
        String suffix = String.valueOf(System.nanoTime() % 1_000_000_000L);
        LoginEntity user = loginRepository.save(LoginEntity.builder()
                .loginId("ncu" + suffix).password("x").name("동시테스트").role(Role.USER).build());
        List<Long> createdIds = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(REQUESTS);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < REQUESTS; i++) {
                futures.add(pool.submit(() -> {
                    start.await(); // 모두 준비된 뒤 한꺼번에 출발
                    ComplainDto.Detail created = complainService.create(user.getId(),
                            new ComplainDto.SaveRequest(ComplainType.SIMPLE, null,
                                    "동시 등록 테스트", "동시 등록 테스트 내용", null, null), null);
                    createdIds.add(created.id());
                    return created.complainNo();
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
            complainRepository.deleteAllById(createdIds);
            loginRepository.deleteById(user.getId());
        }
    }
}
