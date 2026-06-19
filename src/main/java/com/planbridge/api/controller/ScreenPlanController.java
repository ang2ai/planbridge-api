package com.planbridge.api.controller;

import com.planbridge.api.dto.request.ScreenPlanCreateRequest;
import com.planbridge.api.dto.request.ScreenPlanUpdateRequest;
import com.planbridge.api.dto.response.AnalysisQueueResponse;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.dto.response.ScreenPlanResponse;
import com.planbridge.api.service.ScreenPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class ScreenPlanController {

    private final ScreenPlanService screenPlanService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScreenPlanResponse>>> list(
            @RequestParam(required = false) String projectId) {
        if (projectId != null && !projectId.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(screenPlanService.findByProjectId(projectId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(screenPlanService.findAll()));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<ScreenPlanResponse>> get(@PathVariable String planId) {
        return ResponseEntity.ok(ApiResponse.ok(screenPlanService.findById(planId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScreenPlanResponse>> create(
            @Valid @RequestBody ScreenPlanCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("화면 기획서가 생성되었습니다",
                screenPlanService.create(req)));
    }

    @PutMapping("/{planId}")
    public ResponseEntity<ApiResponse<ScreenPlanResponse>> update(
            @PathVariable String planId,
            @RequestBody ScreenPlanUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("화면 기획서가 수정되었습니다",
                screenPlanService.update(planId, req)));
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String planId) {
        screenPlanService.delete(planId);
        return ResponseEntity.ok(ApiResponse.ok("화면 기획서가 삭제되었습니다", null));
    }

    @PostMapping("/{planId}/analyze")
    public ResponseEntity<ApiResponse<AnalysisQueueResponse>> analyze(
            @PathVariable String planId,
            @RequestBody(required = false) Map<String, String> body) {
        String analysisType = (body != null && body.get("analysisType") != null)
                ? body.get("analysisType") : "NEW_PLAN";
        return ResponseEntity.ok(ApiResponse.ok("AI 분석을 시작합니다",
                screenPlanService.requestAnalysis(planId, analysisType)));
    }

    @PostMapping("/{planId}/validate")
    public ResponseEntity<ApiResponse<AnalysisQueueResponse>> validate(
            @PathVariable String planId,
            @RequestBody(required = false) Map<String, String> body) {
        String analysisType = (body != null && body.get("analysisType") != null)
                ? body.get("analysisType") : "CONFLICT_CHECK";
        return ResponseEntity.ok(ApiResponse.ok("충돌 검증을 시작합니다",
                screenPlanService.requestAnalysis(planId, analysisType)));
    }

    @PostMapping("/{planId}/generate")
    public ResponseEntity<ApiResponse<AnalysisQueueResponse>> generate(
            @PathVariable String planId,
            @RequestBody(required = false) Map<String, String> body) {
        String analysisType = (body != null && body.get("analysisType") != null)
                ? body.get("analysisType") : "GENERATE_SPEC";
        return ResponseEntity.ok(ApiResponse.ok("기획서 생성을 시작합니다",
                screenPlanService.requestAnalysis(planId, analysisType)));
    }

    // -----------------------------------------------------------------------
    // GET /api/plans/{id}/export
    // -----------------------------------------------------------------------

    @GetMapping(value = "/{planId}/export", produces = "text/markdown;charset=UTF-8")
    public ResponseEntity<String> export(@PathVariable String planId) {
        String markdown = screenPlanService.exportAsMarkdown(planId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"))
                .body(markdown);
    }
}
