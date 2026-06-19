package com.planbridge.api.controller;

import com.planbridge.api.dto.request.ProjectCreateRequest;
import com.planbridge.api.dto.request.ProjectUpdateRequest;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.dto.response.ComponentResponse;
import com.planbridge.api.dto.response.ProjectResponse;
import com.planbridge.api.service.ComponentService;
import com.planbridge.api.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ComponentService componentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody ProjectCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("프로젝트가 생성되었습니다", projectService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(@PathVariable String id,
                                                               @RequestBody ProjectUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("프로젝트가 수정되었습니다", projectService.update(id, req)));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponse<ProjectResponse>> sync(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Git 동기화를 시작합니다", projectService.triggerSync(id)));
    }

    @GetMapping("/{id}/components")
    public ResponseEntity<ApiResponse<List<ComponentResponse>>> components(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(componentService.findTreeByProjectId(id)));
    }
}
