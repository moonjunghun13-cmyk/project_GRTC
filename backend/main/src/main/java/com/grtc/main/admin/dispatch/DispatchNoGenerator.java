package com.grtc.main.admin.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class DispatchNoGenerator {

    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");

    private final DispatchRepository dispatchRepository;
    private final JdbcTemplate jdbcTemplate;

    // 같은 날짜로 동시에 등록되면 번호가 겹칠 수 있어서 날짜 단위로 잠그고 만든다
    @Transactional(propagation = Propagation.MANDATORY)
    public String next(LocalDate dispatchDate) {
        String ymd = YYMMDD.format(dispatchDate);
        lock(ymd);

        String prefix = "DISP" + ymd + "-";
        int seq = dispatchRepository.findMaxSequence(prefix, prefix.length() + 1) + 1;
        return prefix + String.format("%03d", seq);
    }

    private void lock(String ymd) {
        jdbcTemplate.query("select pg_advisory_xact_lock(?, ?)", rs -> null, 0x4453, Integer.parseInt(ymd));
    }
}
