package com.example.coursework_tc.repository;

import com.example.coursework_tc.model.TrackingSession;
import com.example.coursework_tc.model.enums.TrackingSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingSessionRepository extends JpaRepository<TrackingSession, Long> {

    List<TrackingSession> findByVehicleIdOrderByStartedAtDesc(Long vehicleId);

    Optional<TrackingSession> findByVehicleIdAndStatus(Long vehicleId, TrackingSessionStatus status);

    Optional<TrackingSession> findFirstByRouteIdAndStatusOrderByStartedAtDesc(Long routeId, TrackingSessionStatus status);

    Optional<TrackingSession> findFirstByRouteIdOrderByStartedAtDesc(Long routeId);
}
