package com.planbridge.api.controller;

import com.planbridge.api.dto.request.ChangeRequestCreateRequest;
import com.planbridge.api.dto.request.ChangeRequestUpdateRequest;
import com.planbridge.api.dto.response.AnalysisQueueResponse;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.dto.response.ChangeRequestResponse;
import com.planbridge.api.dto.response.TodoResponse;
import com.planbridge.api.service.ChangeRequestService;
import com.planbridge.api.service.SseEmitterRegistry;
import com.planbridge.api.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/change-requests")
@RequiredArgsConstructor
public class ChangeRequestController {

    private final ChangeRequestService changeRequestService;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final TodoService todoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChangeRequestResponse>>> list(
            @RequestParam(required = false) String projectId) {
        if (projectId != null) {
            return ResponseEntity.ok(ApiResponse.ok(changeRequestService.findByProjectId(projectId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(changeRequestService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChangeRequestResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(changeRequestService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChangeRequestResponse>> create(
            @Valid @RequestBody ChangeRequestCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("변경 요청이 생성되었습니다",
                changeRequestService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChangeRequestResponse>> update(
            @PathVariable String id,
            @RequestBody ChangeRequestUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("변경 요청이 수정되었습니다",
                changeRequestService.update(id, req)));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ApiResponse<AnalysisQueueResponse>> analyze(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("AI 분석을 시작합니다",
                changeRequestService.requestAnalysis(id)));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AnalysisQueueResponse>> status(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(changeRequestService.getAnalysisStatus(id)));
    }

    /**
     * GET /api/change-requests/{id}/todos
     * Lists all TODO items belonging to a ChangeRequest, ordered by sortOrder.
     */
    @GetMapping("/{id}/todos")
    public ResponseEntity<ApiResponse<List<TodoResponse>>> todos(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.findByRequestId(id)));
    }

    /**
     * POST /api/change-requests/{id}/complete
     * Transitions ChangeRequest status to DONE (기획자가 테스트 완료 처리)
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ChangeRequestResponse>> complete(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("변경 요청이 완료 처리되었습니다",
                changeRequestService.complete(id)));
    }

    // SSE 실시간 분석 진행 상태 스트림
    @GetMapping("/{id}/stream")
    public SseEmitter stream(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5분 timeout
        sseEmitterRegistry.register(id, emitter);
        emitter.onCompletion(() -> sseEmitterRegistry.remove(id));
        emitter.onTimeout(() -> sseEmitterRegistry.remove(id));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("requestId", id, "message", "SSE 연결됨")));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
