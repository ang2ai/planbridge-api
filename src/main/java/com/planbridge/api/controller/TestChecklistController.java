package com.planbridge.api.controller;

import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.entity.PbTestChecklist;
import com.planbridge.api.service.TestChecklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TestChecklistController {

    private final TestChecklistService testChecklistService;

    /**
     * GET /api/checklists?policyId=&planId=
     * policyId 또는 planId 중 하나를 필수로 지정한다.
     */
    @GetMapping("/api/checklists")
    public ResponseEntity<ApiResponse<List<PbTestChecklist>>> list(
            @RequestParam(required = false) String policyId,
            @RequestParam(required = false) String planId) {

        List<PbTestChecklist> result;
        if (policyId != null) {
            result = testChecklistService.findByPolicyId(policyId);
        } else if (planId != null) {
            result = testChecklistService.findByPlanId(planId);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("policyId 또는 planId 파라미터가 필요합니다."));
        }
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * POST /api/policies/{policyId}/checklists/generate
     * 정책 기반 체크리스트 자동 생성
     */
    @PostMapping("/api/policies/{policyId}/checklists/generate")
    public ResponseEntity<ApiResponse<List<PbTestChecklist>>> generate(
            @PathVariable String policyId,
            @RequestBody(required = false) Map<String, String> body) {

        String createdBy = (body != null && body.get("createdBy") != null)
                ? body.get("createdBy") : "system";
        List<PbTestChecklist> items = testChecklistService.generateFromPolicy(policyId, createdBy);
        return ResponseEntity.ok(ApiResponse.ok("체크리스트가 생성되었습니다", items));
    }

    /**
     * PUT /api/checklists/{checklistId}
     * 체크리스트 상태 업데이트
     */
    @PutMapping("/api/checklists/{checklistId}")
    public ResponseEntity<ApiResponse<PbTestChecklist>> update(
            @PathVariable String checklistId,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        String checkedBy = body.getOrDefault("checkedBy", "system");

        if (status == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("status 필드가 필요합니다."));
        }

        PbTestChecklist updated = testChecklistService.updateStatus(checklistId, status, checkedBy);
        return ResponseEntity.ok(ApiResponse.ok("체크리스트가 업데이트되었습니다", updated));
    }
}
