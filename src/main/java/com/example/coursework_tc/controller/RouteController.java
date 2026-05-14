package com.example.coursework_tc.controller;

import com.example.coursework_tc.model.Route;
import com.example.coursework_tc.model.Vehicle;
import com.example.coursework_tc.model.enums.RouteStatus;
import com.example.coursework_tc.service.RouteService;
import com.example.coursework_tc.service.UserService;
import com.example.coursework_tc.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;
    private final UserService userService;
    private final VehicleService vehicleService;

    @GetMapping()
    public String getRoutes(Model model, Principal principal) {
        model.addAttribute("routes", routeService.getAllRoutes());
        model.addAttribute("user", userService.getUserByPrincipal(principal));
        return "routes";
    }

    @GetMapping("/{id}")
    public String getRouteDetails(Model model, @PathVariable Long id, Principal principal) {
        Route route = routeService.getRouteById(id);
        String status;
        if (route.getStatus() == RouteStatus.ACTIVE) {
            status = "Заказ создан и ожидает обработки.";
        } else if (route.getStatus() == RouteStatus.IN_PROCESS) {
            status = "Заказ находится в процессе доставки.";
        } else { status = "Заказ успешно доставлен."; }

        model.addAttribute("route", route);
        model.addAttribute("status", status);
        model.addAttribute("user", userService.getUserByPrincipal(principal));
        if (route.getOrder() != null) {
            Vehicle assignedVehicle = vehicleService.findVehicleByOrderId(route.getOrder().getId());
            if (assignedVehicle != null) {
                model.addAttribute("assignedVehicleId", assignedVehicle.getId());
            }
        }
        return "route-details";
    }
}
