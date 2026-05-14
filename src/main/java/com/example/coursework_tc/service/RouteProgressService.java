package com.example.coursework_tc.service;

import com.example.coursework_tc.dto.route.RouteProgressResponse;

public interface RouteProgressService {

    RouteProgressResponse getProgress(Long routeId);
}
