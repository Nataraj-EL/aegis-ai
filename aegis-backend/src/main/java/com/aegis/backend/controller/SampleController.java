package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sample")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class SampleController {

    public SampleController() {
        // Default constructor
    }

    @GetMapping("/protected")
    public ResponseEntity<ApiResponse<String>> getProtectedData() {
        return ResponseEntity.ok(ApiResponse.success("Access Granted: This is a protected endpoint.", "Success"));
    }
}
