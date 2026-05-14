package com.example.coursework_tc.controller;

import com.example.coursework_tc.dto.route.RouteProgressResponse;
import com.example.coursework_tc.service.RouteProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class RouteTrackingController {

    private final RouteProgressService routeProgressService;

    @GetMapping("/{routeId}/progress")
    public RouteProgressResponse getRouteProgress(@PathVariable Long routeId) {
        return routeProgressService.getProgress(routeId);
    }
}
