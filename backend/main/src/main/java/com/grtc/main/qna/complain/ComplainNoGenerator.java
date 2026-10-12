package com.grtc.main.qna.complain;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ComplainNoGenerator {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int LOCK_GROUP = 0x434D;

    private final ComplainRepository complainRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public String next() {
        String date = LocalDate.now().format(NO_DATE);
        String prefix = "CM" + date + "-";

        jdbcTemplate.query("select pg_advisory_xact_lock(?, ?)",
                (ResultSetExtractor<Void>) rs -> null, LOCK_GROUP, Integer.parseInt(date));

        int last = complainRepository.findMaxSequence(prefix, prefix.length() + 1);
        return prefix + String.format("%03d", last + 1);
    }
}
