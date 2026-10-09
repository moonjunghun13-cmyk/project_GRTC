package com.grtc.main.admin.operation;

import com.grtc.main.admin.vehicle.VehicleEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 현재 운행 중인 열차(편성)의 위치 정보. 차량 1대당 1건.
@Entity
@Table(name = "train_operation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OperationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TrainDirection direction;

    // 열차가 지금 위치한 역
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "current_station_id", nullable = false)
    private StationEntity currentStation;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        updatedAt = LocalDateTime.now();
    }
}
