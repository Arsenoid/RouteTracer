package com.example.coursework_tc.dto.route;

import java.time.Instant;

public record RouteProgressResponse(
        Long routeId,
        Long sessionId,
        Double plannedDistanceKm,
        Double coveredDistanceKm,
        Double remainingDistanceKm,
        Double progressPercent,
        Instant lastPointAt,
        Integer etaMinutes
) {
}
