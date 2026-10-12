package com.grtc.main.global.init;

import com.grtc.main.qna.complain.ComplainStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComplainStatusConstraintUpdater implements ApplicationRunner {

    private static final String FIND_SQL =
            "select pg_get_constraintdef(oid) from pg_constraint "
                    + "where conname = 'complain_status_check' and conrelid = to_regclass('complain')";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        String allowed = Arrays.stream(ComplainStatus.values())
                .map(s -> "'" + s.name() + "'")
                .collect(Collectors.joining(", "));
        try {
            List<String> definitions = jdbcTemplate.queryForList(FIND_SQL, String.class);
            if (definitions.isEmpty()) {
                return; // 제약이 없으면 막는 것도 없다
            }
            String definition = definitions.get(0);
            boolean upToDate = Arrays.stream(ComplainStatus.values())
                    .allMatch(s -> definition.contains("'" + s.name() + "'"));
            if (upToDate) {
                return;
            }
            jdbcTemplate.execute("alter table complain "
                    + "drop constraint complain_status_check, "
                    + "add constraint complain_status_check check (status in (" + allowed + "))");
            log.info("[init] complain_status_check 제약을 갱신했습니다. 허용 상태: {}", allowed);
        } catch (DataAccessException e) {
            log.warn("[init] complain_status_check 제약 갱신 실패. 민원 철회가 저장되지 않을 수 있습니다. "
                    + "직접 실행: alter table complain drop constraint complain_status_check, "
                    + "add constraint complain_status_check check (status in ({})); 원인: {}", allowed, e.getMessage());
        }
    }
}
