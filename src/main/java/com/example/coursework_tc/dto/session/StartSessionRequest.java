package com.example.coursework_tc.dto.session;

import com.example.coursework_tc.model.enums.TelemetrySource;
import jakarta.validation.constraints.NotNull;

public record StartSessionRequest(
        @NotNull Long vehicleId,
        @NotNull TelemetrySource source,
        @NotNull Long routeId
) {
}
