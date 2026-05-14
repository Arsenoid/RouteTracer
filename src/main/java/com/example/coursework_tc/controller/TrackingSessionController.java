package com.example.coursework_tc.controller;

import com.example.coursework_tc.dto.session.StartSessionRequest;
import com.example.coursework_tc.dto.session.TrackingSessionResponse;
import com.example.coursework_tc.service.TrackingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class TrackingSessionController {

    private final TrackingSessionService sessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingSessionResponse startSession(@Valid @RequestBody StartSessionRequest request) {
        return TrackingSessionResponse.from(
                sessionService.startSession(request.vehicleId(), request.source(), request.routeId())
        );
    }

    @PostMapping("/{id}/stop")
    public TrackingSessionResponse stopSession(@PathVariable Long id) {
        return TrackingSessionResponse.from(sessionService.stopSession(id));
    }

    @GetMapping("/{id}")
    public TrackingSessionResponse getSession(@PathVariable Long id) {
        return TrackingSessionResponse.from(sessionService.findById(id));
    }

    @GetMapping
    public List<TrackingSessionResponse> listSessions(@RequestParam Long vehicleId) {
        return sessionService.findByVehicle(vehicleId)
                .stream()
                .map(TrackingSessionResponse::from)
                .toList();
    }
}
