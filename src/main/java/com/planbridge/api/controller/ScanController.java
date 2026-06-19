package com.planbridge.api.controller;

import com.planbridge.api.dto.request.ScanDataRequest;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.service.ComponentService;
import com.planbridge.api.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ScanController {

    private final ComponentService componentService;
    private final ScanService scanService;

    /**
     * 익스텐션에서 페이지 스캔 데이터 수신
     * POST /api/projects/:id/scan
     */
    @PostMapping("/api/projects/{projectId}/scan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> receiveScan(
            @PathVariable String projectId,
            @RequestBody ScanDataRequest req) {
        Map<String, Object> result = componentService.processScan(projectId, req);
        return ResponseEntity.ok(ApiResponse.ok("스캔 데이터가 처리되었습니다", result));
    }

    /**
     * 프로젝트 전체 재스캔 요청
     * POST /api/scan/{projectId}/full
     */
    @PostMapping("/api/scan/{projectId}/full")
    public ResponseEntity<ApiResponse<Object>> requestFullScan(@PathVariable String projectId) {
        var result = scanService.requestFullScan(projectId);
        return ResponseEntity.ok(ApiResponse.ok("전체 스캔 작업이 큐에 등록되었습니다", result));
    }
}
