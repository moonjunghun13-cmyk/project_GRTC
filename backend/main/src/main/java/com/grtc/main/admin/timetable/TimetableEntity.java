package com.grtc.main.admin.timetable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

// 입·출고 시간표 한 줄 (요일 구분 + 출고/입고 + 열번 + 시각)
//  - 원본: 광주 도시철도 입·출고 및 녹동열차 시간표 (팀 노션 참고자료의 입출고_시간표.xlsx)
//  - resources/data/depot-timetable.csv 에서 처음 한 번 불러온다.
@Entity
@Table(name = "depot_timetable",
        uniqueConstraints = @UniqueConstraint(columnNames = {"day_type", "move_type", "seq"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TimetableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20)
    private DayType dayType;

    @Enumerated(EnumType.STRING)
    @Column(name = "move_type", nullable = false, length = 20)
    private MoveType moveType;

    // 원본 표의 순서 (1부터)
    @Column(nullable = false)
    private int seq;

    // 열번. 예: 1901
    @Column(name = "train_no", nullable = false, length = 10)
    private String trainNo;

    // 출고 또는 입고 시각. 0시대(예: 0:05)는 그날 운행의 마지막 열차다.
    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;
}
