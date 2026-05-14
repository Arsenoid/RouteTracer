package com.example.coursework_tc.service.impl;

import com.example.coursework_tc.dto.route.RouteProgressResponse;
import com.example.coursework_tc.model.Route;
import com.example.coursework_tc.model.TelemetryPoint;
import com.example.coursework_tc.model.TrackingSession;
import com.example.coursework_tc.model.enums.TrackingSessionStatus;
import com.example.coursework_tc.repository.RouteRepository;
import com.example.coursework_tc.repository.TelemetryPointRepository;
import com.example.coursework_tc.repository.TrackingSessionRepository;
import com.example.coursework_tc.service.RouteProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteProgressServiceImpl implements RouteProgressService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final RouteRepository routeRepository;
    private final TrackingSessionRepository sessionRepository;
    private final TelemetryPointRepository telemetryPointRepository;

    @Override
    public RouteProgressResponse getProgress(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));

        double planned = route.getDistance() != null && route.getDistance() > 0
                ? route.getDistance()
                : 0.0;

        TrackingSession session = sessionRepository
                .findFirstByRouteIdAndStatusOrderByStartedAtDesc(routeId, TrackingSessionStatus.ACTIVE)
                .orElseGet(() -> sessionRepository.findFirstByRouteIdOrderByStartedAtDesc(routeId).orElse(null));

        if (session == null) {
            return new RouteProgressResponse(
                    routeId,
                    null,
                    round1(planned),
                    0.0,
                    round1(Math.max(0, planned)),
                    0.0,
                    null,
                    null
            );
        }

        if (planned <= 0) {
            return new RouteProgressResponse(
                    routeId,
                    session.getId(),
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    null,
                    null
            );
        }

        List<TelemetryPoint> points = telemetryPointRepository.findBySessionIdOrderByRecordedAtAsc(session.getId());
        double covered = sumPlausiblePathKm(points);
        covered = Math.min(covered, planned * 1.5);

        double remaining = Math.max(0, planned - covered);
        double percent = planned > 0 ? Math.min(100.0, (covered / planned) * 100.0) : 0.0;

        Instant lastAt = points.isEmpty() ? null : points.getLast().getRecordedAt();
        Integer eta = estimateEtaMinutes(remaining, points);

        return new RouteProgressResponse(
                routeId,
                session.getId(),
                round1(planned),
                round1(covered),
                round1(remaining),
                round1(percent),
                lastAt,
                eta
        );
    }

    private static Integer estimateEtaMinutes(double remainingKm, List<TelemetryPoint> points) {
        if (remainingKm <= 0.01 || points.size() < 2) {
            return null;
        }
        Double avgKmh = recentAverageSpeedKmh(points);
        if (avgKmh == null || avgKmh < 1.0) {
            return null;
        }
        double hours = remainingKm / avgKmh;
        return (int) Math.ceil(hours * 60.0);
    }

    /**
     * Average speed over the last up to 10 plausible segments (km/h).
     */
    private static Double recentAverageSpeedKmh(List<TelemetryPoint> points) {
        int n = points.size();
        double distKm = 0;
        long seconds = 0;
        int segments = 0;
        for (int i = n - 1; i > 0 && segments < 10; i--) {
            TelemetryPoint b = points.get(i);
            TelemetryPoint a = points.get(i - 1);
            if (!isPlausibleSegment(a, b)) {
                continue;
            }
            distKm += haversineKm(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
            seconds += Math.max(1, Duration.between(a.getRecordedAt(), b.getRecordedAt()).getSeconds());
            segments++;
        }
        if (seconds < 5 || distKm < 0.005) {
            return null;
        }
        return (distKm / seconds) * 3600.0;
    }

    private static double sumPlausiblePathKm(List<TelemetryPoint> points) {
        if (points.size() < 2) {
            return 0;
        }
        double sum = 0;
        for (int i = 1; i < points.size(); i++) {
            TelemetryPoint a = points.get(i - 1);
            TelemetryPoint b = points.get(i);
            if (isPlausibleSegment(a, b)) {
                sum += haversineKm(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
            }
        }
        return sum;
    }

    /**
     * Reject GPS jumps: implausible speed between two fixes.
     */
    private static boolean isPlausibleSegment(TelemetryPoint a, TelemetryPoint b) {
        double km = haversineKm(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
        long sec = Math.max(1, Duration.between(a.getRecordedAt(), b.getRecordedAt()).getSeconds());
        double maxKm = (sec / 3600.0) * 180.0 + 0.3;
        return km <= maxKm;
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double r1 = Math.toRadians(lat1);
        double r2 = Math.toRadians(lat2);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(r1) * Math.cos(r2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return EARTH_RADIUS_KM * c;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
