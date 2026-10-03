package com.grtc.main.admin.vehicle;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

// 차량(편성) 정보 엔티티
@Entity
@Table(name = "vehicle")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 차량번호(편성 이름). 예: 101편성
    @Column(name = "vehicle_no", nullable = false, unique = true, length = 30)
    private String vehicleNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    // 최근 점검일
    @Column(name = "last_inspection_date")
    private LocalDate lastInspectionDate;

    public void update(String vehicleNo, VehicleStatus status, LocalDate lastInspectionDate) {
        this.vehicleNo = vehicleNo;
        this.status = status;
        this.lastInspectionDate = lastInspectionDate;
    }
}
