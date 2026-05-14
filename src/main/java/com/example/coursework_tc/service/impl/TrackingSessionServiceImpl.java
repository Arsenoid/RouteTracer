package com.example.coursework_tc.service.impl;

import com.example.coursework_tc.exception.SessionAlreadyActiveException;
import com.example.coursework_tc.exception.SessionNotFoundException;
import com.example.coursework_tc.exception.VehicleNotFoundException;
import com.example.coursework_tc.model.Route;
import com.example.coursework_tc.model.TrackingSession;
import com.example.coursework_tc.model.Vehicle;
import com.example.coursework_tc.model.enums.RouteStatus;
import com.example.coursework_tc.model.enums.TelemetrySource;
import com.example.coursework_tc.model.enums.TrackingSessionStatus;
import com.example.coursework_tc.repository.RouteRepository;
import com.example.coursework_tc.repository.TrackingSessionRepository;
import com.example.coursework_tc.repository.VehicleRepository;
import com.example.coursework_tc.service.TrackingSessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingSessionServiceImpl implements TrackingSessionService {

    private final TrackingSessionRepository sessionRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public TrackingSession startSession(Long vehicleId, TelemetrySource source, Long routeId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));

        if (route.getStatus() != RouteStatus.IN_PROCESS) {
            throw new IllegalArgumentException("Route must be IN_PROCESS to start tracking");
        }
        if (route.getOrder() == null) {
            throw new IllegalArgumentException("Route has no active order");
        }
        if (vehicle.getOrderId() == null || !vehicle.getOrderId().equals(route.getOrder().getId())) {
            throw new IllegalArgumentException("Vehicle is not assigned to the specified route");
        }

        sessionRepository.findByVehicleIdAndStatus(vehicleId, TrackingSessionStatus.ACTIVE)
                .ifPresent(s -> { throw new SessionAlreadyActiveException(vehicleId); });

        TrackingSession session = new TrackingSession();
        session.setVehicle(vehicle);
        session.setRoute(route);
        session.setSource(source);
        session.setStatus(TrackingSessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());

        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public TrackingSession stopSession(Long sessionId) {
        TrackingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        session.setStatus(TrackingSessionStatus.COMPLETED);
        session.setEndedAt(Instant.now());

        return sessionRepository.save(session);
    }

    @Override
    public TrackingSession findById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    @Override
    public List<TrackingSession> findByVehicle(Long vehicleId) {
        return sessionRepository.findByVehicleIdOrderByStartedAtDesc(vehicleId);
    }
}
