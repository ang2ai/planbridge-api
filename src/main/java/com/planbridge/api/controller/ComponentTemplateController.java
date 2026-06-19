package com.planbridge.api.controller;

import com.planbridge.api.dto.request.ComponentTemplateCreateRequest;
import com.planbridge.api.dto.request.ComponentTemplateUpdateRequest;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.dto.response.ComponentTemplateResponse;
import com.planbridge.api.service.ComponentTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ComponentTemplateController {

    private final ComponentTemplateService componentTemplateService;

    @GetMapping("/api/projects/{projectId}/templates")
    public ResponseEntity<ApiResponse<List<ComponentTemplateResponse>>> getByProject(
            @PathVariable String projectId) {
        return ResponseEntity.ok(ApiResponse.ok(componentTemplateService.findByProjectId(projectId)));
    }

    @GetMapping("/api/templates/{templateId}")
    public ResponseEntity<ApiResponse<ComponentTemplateResponse>> get(
            @PathVariable String templateId) {
        return ResponseEntity.ok(ApiResponse.ok(componentTemplateService.findById(templateId)));
    }

    @PostMapping("/api/templates")
    public ResponseEntity<ApiResponse<ComponentTemplateResponse>> create(
            @RequestBody ComponentTemplateCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                "템플릿이 생성되었습니다",
                componentTemplateService.create(req)));
    }

    @PutMapping("/api/templates/{templateId}")
    public ResponseEntity<ApiResponse<ComponentTemplateResponse>> update(
            @PathVariable String templateId,
            @RequestBody ComponentTemplateUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                "템플릿이 수정되었습니다",
                componentTemplateService.update(templateId, req)));
    }

    @DeleteMapping("/api/templates/{templateId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String templateId) {
        componentTemplateService.delete(templateId);
        return ResponseEntity.ok(ApiResponse.ok("템플릿이 삭제되었습니다", null));
    }

    @PostMapping("/api/templates/{templateId}/use")
    public ResponseEntity<ApiResponse<ComponentTemplateResponse>> incrementUsage(
            @PathVariable String templateId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "사용 횟수가 증가되었습니다",
                componentTemplateService.incrementUsage(templateId)));
    }
}
