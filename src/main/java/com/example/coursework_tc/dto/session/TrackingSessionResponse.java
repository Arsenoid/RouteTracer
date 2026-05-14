package com.example.coursework_tc.dto.session;

import com.example.coursework_tc.model.TrackingSession;
import com.example.coursework_tc.model.enums.TelemetrySource;
import com.example.coursework_tc.model.enums.TrackingSessionStatus;

import java.time.Instant;

public record TrackingSessionResponse(
        Long id,
        Long vehicleId,
        Long routeId,
        TelemetrySource source,
        TrackingSessionStatus status,
        Instant startedAt,
        Instant endedAt
) {
    public static TrackingSessionResponse from(TrackingSession session) {
        return new TrackingSessionResponse(
                session.getId(),
                session.getVehicle().getId(),
                session.getRoute() != null ? session.getRoute().getId() : null,
                session.getSource(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}
