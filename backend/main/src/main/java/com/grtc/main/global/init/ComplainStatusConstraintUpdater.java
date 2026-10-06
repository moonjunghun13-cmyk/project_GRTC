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

// complain.status 의 CHECK 제약을 ComplainStatus enum 과 맞춘다. (서버 시작 때 한 번, 이미 맞으면 아무것도 안 함)
//  - Hibernate 는 테이블을 처음 만들 때 enum 값 목록으로 CHECK 제약(complain_status_check)을 건다.
//  - 하지만 ddl-auto: update 는 나중에 enum 값이 늘어나도 이 제약을 고쳐 주지 않는다.
//    그래서 예전에 만들어진 DB 에서는 새 상태(예: WITHDRAWN 철회)를 저장할 때 제약 위반으로 실패한다.
//  - 팀원마다 DB 를 따로 쓰므로, 각자 SQL 을 돌리지 않아도 되게 여기서 자동으로 고친다.
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
            // 지우고 다시 거는 것을 한 문장으로 처리한다. (중간에 실패해도 제약이 사라진 채로 남지 않는다)
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
