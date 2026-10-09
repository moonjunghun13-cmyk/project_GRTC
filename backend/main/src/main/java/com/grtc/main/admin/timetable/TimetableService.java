package com.grtc.main.admin.timetable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// 입·출고 시간표 조회 / 최초 적재
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    static final String CSV = "data/depot-timetable.csv";

    private final TimetableRepository timetableRepository;

    // 요일 구분별 시간표
    public TimetableDto.Response get(DayType dayType) {
        return response(dayType, null);
    }

    // 날짜에 맞는 시간표 (평일/토요일/휴일 자동 선택)
    public TimetableDto.Response get(LocalDate date) {
        return response(DayType.of(date), date);
    }

    public List<TimetableEntity> rows(DayType dayType) {
        return timetableRepository.findAllByDayTypeOrderBySeqAsc(dayType);
    }

    // 시간표 테이블이 비어 있으면 CSV 를 불러와 저장한다. 저장한 줄 수를 돌려준다.
    @Transactional
    public int loadIfEmpty() {
        if (timetableRepository.count() > 0) {
            return 0;
        }
        List<TimetableEntity> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(CSV).getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // 머리글
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] c = line.split(",");
                rows.add(TimetableEntity.builder()
                        .dayType(DayType.valueOf(c[0].trim()))
                        .moveType(MoveType.valueOf(c[1].trim()))
                        .seq(Integer.parseInt(c[2].trim()))
                        .trainNo(c[3].trim())
                        .scheduledTime(LocalTime.parse(c[4].trim()))
                        .build());
            }
        } catch (IOException e) {
            throw new IllegalStateException("입·출고 시간표 파일을 읽을 수 없습니다: " + CSV, e);
        }
        timetableRepository.saveAll(rows);
        log.info("[timetable] 입·출고 시간표 {}줄 등록", rows.size());
        return rows.size();
    }

    private TimetableDto.Response response(DayType dayType, LocalDate date) {
        List<TimetableEntity> rows = rows(dayType);
        return new TimetableDto.Response(dayType, dayType.getLabel(), date,
                rows.stream().filter(r -> r.getMoveType() == MoveType.DEPART).map(TimetableDto.Row::from).toList(),
                rows.stream().filter(r -> r.getMoveType() == MoveType.RETURN).map(TimetableDto.Row::from).toList());
    }
}
