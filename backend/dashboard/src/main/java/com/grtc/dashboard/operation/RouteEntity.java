package com.grtc.dashboard.operation;

import jakarta.persistence.*;
import lombok.*;

// 노선 (예: 광주 도시철도 1호선)
@Entity
@Table(name = "route")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteStatus status;

    // 오늘 운행 횟수 (대시보드 '운행 횟수')
    @Column(name = "daily_run_count", nullable = false)
    private int dailyRunCount;
}
