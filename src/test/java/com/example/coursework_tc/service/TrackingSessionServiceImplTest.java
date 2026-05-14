package com.example.coursework_tc.service;

import com.example.coursework_tc.exception.SessionAlreadyActiveException;
import com.example.coursework_tc.exception.SessionNotFoundException;
import com.example.coursework_tc.exception.VehicleNotFoundException;
import com.example.coursework_tc.model.Order;
import com.example.coursework_tc.model.Route;
import com.example.coursework_tc.model.TrackingSession;
import com.example.coursework_tc.model.Vehicle;
import com.example.coursework_tc.model.enums.RouteStatus;
import com.example.coursework_tc.model.enums.TelemetrySource;
import com.example.coursework_tc.model.enums.TrackingSessionStatus;
import com.example.coursework_tc.repository.RouteRepository;
import com.example.coursework_tc.repository.TrackingSessionRepository;
import com.example.coursework_tc.repository.VehicleRepository;
import com.example.coursework_tc.service.impl.TrackingSessionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TrackingSessionServiceImplTest {

    @Mock TrackingSessionRepository sessionRepository;
    @Mock VehicleRepository vehicleRepository;
    @Mock RouteRepository routeRepository;

    @InjectMocks TrackingSessionServiceImpl service;

    @Test
    void startSession_success_createsActiveSession() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setOrderId(100L);
        Route route = new Route();
        route.setId(9L);
        route.setStatus(RouteStatus.IN_PROCESS);
        Order order = new Order();
        order.setId(100L);
        route.setOrder(order);
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));
        given(routeRepository.findById(9L)).willReturn(Optional.of(route));
        given(sessionRepository.findByVehicleIdAndStatus(1L, TrackingSessionStatus.ACTIVE))
                .willReturn(Optional.empty());
        given(sessionRepository.save(any())).willAnswer(inv -> {
            TrackingSession s = inv.getArgument(0);
            s.setId(7L);
            return s;
        });

        TrackingSession result = service.startSession(1L, TelemetrySource.BROWSER, 9L);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getStatus()).isEqualTo(TrackingSessionStatus.ACTIVE);
        assertThat(result.getSource()).isEqualTo(TelemetrySource.BROWSER);
        assertThat(result.getStartedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
        assertThat(result.getVehicle()).isSameAs(vehicle);
        assertThat(result.getRoute()).isSameAs(route);
    }

    @Test
    void startSession_throwsVehicleNotFoundException_whenVehicleDoesNotExist() {
        given(vehicleRepository.findById(42L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.startSession(42L, TelemetrySource.BROWSER, 9L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("42");
        then(sessionRepository).shouldHaveNoInteractions();
    }

    @Test
    void startSession_throwsSessionAlreadyActiveException_whenActiveSessionExists() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setOrderId(100L);
        Route route = new Route();
        route.setId(9L);
        route.setStatus(RouteStatus.IN_PROCESS);
        Order order = new Order();
        order.setId(100L);
        route.setOrder(order);
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));
        given(routeRepository.findById(9L)).willReturn(Optional.of(route));
        TrackingSession existing = new TrackingSession();
        existing.setId(5L);
        given(sessionRepository.findByVehicleIdAndStatus(1L, TrackingSessionStatus.ACTIVE))
                .willReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.startSession(1L, TelemetrySource.BROWSER, 9L))
                .isInstanceOf(SessionAlreadyActiveException.class)
                .hasMessageContaining("1");
        then(sessionRepository).should(never()).save(any());
    }

    @Test
    void stopSession_success_setsCompletedStatusAndEndedAt() {
        TrackingSession session = new TrackingSession();
        session.setId(10L);
        session.setStatus(TrackingSessionStatus.ACTIVE);
        given(sessionRepository.findById(10L)).willReturn(Optional.of(session));
        given(sessionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        TrackingSession result = service.stopSession(10L);
        Instant after = Instant.now();

        assertThat(result.getStatus()).isEqualTo(TrackingSessionStatus.COMPLETED);
        assertThat(result.getEndedAt()).isBetween(before, after);
    }

    @Test
    void stopSession_throwsSessionNotFoundException_whenSessionDoesNotExist() {
        given(sessionRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.stopSession(99L))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findById_throwsSessionNotFoundException_whenSessionDoesNotExist() {
        given(sessionRepository.findById(7L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(7L))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("7");
    }
}
