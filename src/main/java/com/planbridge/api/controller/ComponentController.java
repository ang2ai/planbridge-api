package com.planbridge.api.controller;

import com.planbridge.api.dto.request.ComponentResolveRequest;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.dto.response.ComponentResponse;
import com.planbridge.api.service.ComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComponentResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(componentService.findById(id)));
    }

    @PostMapping("/resolve")
    public ResponseEntity<ApiResponse<ComponentResponse>> resolve(@RequestBody ComponentResolveRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(componentService.resolve(req)));
    }
}
