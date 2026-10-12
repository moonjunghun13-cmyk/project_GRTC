package com.grtc.main.admin.operation;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "station",
        uniqueConstraints = @UniqueConstraint(columnNames = {"route_id", "seq"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;

    @Column(nullable = false)
    private int seq;

    @Column(nullable = false, length = 50)
    private String name;
}
