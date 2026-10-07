package com.grtc.main.qna.complain;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// 민원번호 생성: CM + 접수일(yyMMdd) + - + 그날의 일련번호(3자리, 999 를 넘으면 자릿수가 늘어난다). 예) CM261007-001
//  - 일련번호는 "그날 가장 큰 번호 + 1" 이다.
//    (예전의 "그날 민원 수 + 1" 은 중간 번호가 지워져 있으면 이미 있는 번호를 다시 만들어 등록이 실패했다.)
//  - 두 사람이 동시에 등록해도 같은 번호가 나오지 않도록, 같은 날짜의 번호 생성은 한 번에 하나씩만 하게 잠근다.
//    잠금은 민원을 저장하는 트랜잭션이 끝날 때(커밋/롤백) 자동으로 풀린다. (PostgreSQL advisory lock)
//  - 그래서 반드시 민원을 저장하는 트랜잭션 안에서 불러야 한다. (사용자 등록, 관리자 등록이 함께 쓴다)
@Component
@RequiredArgsConstructor
public class ComplainNoGenerator {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int LOCK_GROUP = 0x434D; // 다른 용도의 잠금과 겹치지 않게 하는 구분값 ("CM")

    private final ComplainRepository complainRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public String next() {
        String date = LocalDate.now().format(NO_DATE);
        String prefix = "CM" + date + "-";

        // 같은 날짜의 번호를 만드는 다른 요청이 끝날 때까지 기다린다.
        jdbcTemplate.query("select pg_advisory_xact_lock(?, ?)",
                (ResultSetExtractor<Void>) rs -> null, LOCK_GROUP, Integer.parseInt(date));

        int last = complainRepository.findMaxSequence(prefix, prefix.length() + 1);
        return prefix + String.format("%03d", last + 1);
    }
}
