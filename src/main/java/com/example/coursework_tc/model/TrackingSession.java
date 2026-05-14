package com.example.coursework_tc.model;

import com.example.coursework_tc.model.enums.TelemetrySource;
import com.example.coursework_tc.model.enums.TrackingSessionStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "tracking_sessions", indexes = {
        @Index(name = "idx_sessions_vehicle", columnList = "vehicle_id,started_at")
})
public class TrackingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TelemetrySource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TrackingSessionStatus status;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant endedAt;
}
