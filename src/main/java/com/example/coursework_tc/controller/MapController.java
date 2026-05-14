package com.example.coursework_tc.controller;

import com.example.coursework_tc.model.Vehicle;
import com.example.coursework_tc.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/map")
public class MapController {

    private final VehicleService vehicleService;

    @GetMapping
    public String mapPage(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long routeId,
            Model model
    ) {
        if (vehicleId != null) {
            try {
                Vehicle vehicle = vehicleService.findVehicleById(vehicleId);
                model.addAttribute("vehicle", vehicle);
            } catch (Exception ignored) {
                // Vehicle not found — continue without it; JS will handle missing vehicleId gracefully
            }
        }
        model.addAttribute("vehicleId", vehicleId);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("routeId", routeId);
        return "map";
    }
}
