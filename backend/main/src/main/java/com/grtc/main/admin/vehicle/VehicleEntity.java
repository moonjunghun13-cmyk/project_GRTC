package com.grtc.main.admin.vehicle;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @Column(name = "vehicle_no", nullable = false, unique = true, length = 30)
    private String vehicleNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Column(name = "last_inspection_date")
    private LocalDate lastInspectionDate;

    public void update(String vehicleNo, VehicleStatus status, LocalDate lastInspectionDate) {
        this.vehicleNo = vehicleNo;
        this.status = status;
        this.lastInspectionDate = lastInspectionDate;
    }
}
