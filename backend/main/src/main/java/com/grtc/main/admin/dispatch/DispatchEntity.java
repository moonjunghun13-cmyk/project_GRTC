package com.grtc.main.admin.dispatch;

import com.grtc.main.admin.vehicle.VehicleEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// 배차(어느 날, 어느 차량을, 누가, 몇 시에 몇 시까지 운행하는지) 엔티티
@Entity
@Table(name = "dispatch")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DispatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 배차번호. 예: DISP260930-001 (DISP + 배차일 yyMMdd + 일련번호)
    @Column(name = "dispatch_no", nullable = false, unique = true, length = 30)
    private String dispatchNo;

    @Column(name = "dispatch_date", nullable = false)
    private LocalDate dispatchDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @Column(name = "driver_name", nullable = false, length = 30)
    private String driverName;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DispatchStatus status;

    // 비고. 예: 평일 배차, 운전자 대기, 도착시간 조정
    @Column(length = 200)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void update(VehicleEntity vehicle, LocalDate dispatchDate, String driverName,
                       LocalTime departureTime, LocalTime arrivalTime, String remark,
                       DispatchStatus status) {
        this.vehicle = vehicle;
        this.dispatchDate = dispatchDate;
        this.driverName = driverName;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.remark = remark;
        this.status = status;
    }

    public void cancel() {
        this.status = DispatchStatus.CANCELLED;
    }
}
