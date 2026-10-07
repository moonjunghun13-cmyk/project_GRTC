package com.grtc.main.admin.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// 배차번호 생성: DISP + 배차일(yyMMdd) + - + 그날의 일련번호(3자리, 999 를 넘으면 자릿수가 늘어난다). 예) DISP260930-001
//  - 일련번호는 "그 배차일의 가장 큰 번호 + 1" 이다.
//    ("그 배차일의 배차 수 + 1" 로 만들면, 배차를 삭제해 중간 번호가 비었을 때 이미 있는 번호를 다시 만들어 등록이 실패한다)
//  - 두 사람이 동시에 등록해도 같은 번호가 나오지 않도록, 같은 배차일의 번호 생성은 한 번에 하나씩만 하게 잠근다.
//    잠금은 배차를 저장하는 트랜잭션이 끝날 때(커밋/롤백) 자동으로 풀린다. (PostgreSQL advisory lock)
//  - 그래서 반드시 배차를 저장하는 트랜잭션 안에서 불러야 한다. (민원번호의 ComplainNoGenerator 와 같은 방식)
@Component
@RequiredArgsConstructor
public class DispatchNoGenerator {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int LOCK_GROUP = 0x4453; // 다른 용도의 잠금과 겹치지 않게 하는 구분값 ("DS")

    private final DispatchRepository dispatchRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public String next(LocalDate dispatchDate) {
        String date = dispatchDate.format(NO_DATE);
        String prefix = "DISP" + date + "-";

        // 같은 배차일의 번호를 만드는 다른 요청이 끝날 때까지 기다린다.
        jdbcTemplate.query("select pg_advisory_xact_lock(?, ?)",
                (ResultSetExtractor<Void>) rs -> null, LOCK_GROUP, Integer.parseInt(date));

        int last = dispatchRepository.findMaxSequence(prefix, prefix.length() + 1);
        return prefix + String.format("%03d", last + 1);
    }
}
