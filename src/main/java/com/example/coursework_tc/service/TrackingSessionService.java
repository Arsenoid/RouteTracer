package com.example.coursework_tc.service;

import com.example.coursework_tc.model.TrackingSession;
import com.example.coursework_tc.model.enums.TelemetrySource;

import java.util.List;

public interface TrackingSessionService {

    TrackingSession startSession(Long vehicleId, TelemetrySource source, Long routeId);

    TrackingSession stopSession(Long sessionId);

    TrackingSession findById(Long sessionId);

    List<TrackingSession> findByVehicle(Long vehicleId);
}
