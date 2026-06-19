package com.planbridge.api.controller;

import com.planbridge.api.dto.request.PolicyCreateRequest;
import com.planbridge.api.dto.request.PolicyUpdateRequest;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.dto.response.PolicyImpactResponse;
import com.planbridge.api.dto.response.PolicyResponse;
import com.planbridge.api.service.AiPolicyService;
import com.planbridge.api.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
    private final AiPolicyService aiPolicyService;

    // 컴포넌트에 적용된 정책 조회 (직접 + 상속)
    @GetMapping("/api/components/{componentId}/policies")
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> getByComponent(@PathVariable String componentId) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.findByComponent(componentId)));
    }

    @GetMapping("/api/policies/{id}")
    public ResponseEntity<ApiResponse<PolicyResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.findById(id)));
    }

    @PostMapping("/api/policies")
    public ResponseEntity<ApiResponse<PolicyResponse>> create(@Valid @RequestBody PolicyCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("정책이 생성되었습니다", policyService.create(req)));
    }

    @PutMapping("/api/policies/{id}")
    public ResponseEntity<ApiResponse<PolicyResponse>> update(@PathVariable String id,
                                                               @RequestBody PolicyUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("정책이 수정되었습니다", policyService.update(id, req)));
    }

    @DeleteMapping("/api/policies/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        policyService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("정책이 삭제되었습니다", null));
    }

    @GetMapping("/api/policies/search")
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> search(
            @RequestParam(required = false, defaultValue = "") String projectId,
            @RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.search(projectId, q)));
    }

    @GetMapping("/api/policies/{id}/history")
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> history(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.findHistoryByPolicy(id)));
    }

    @GetMapping("/api/policies/{id}/impact")
    public ResponseEntity<ApiResponse<PolicyImpactResponse>> impact(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getImpact(id)));
    }

    // -----------------------------------------------------------------------
    // POST /api/policies/{id}/to-prompt
    // -----------------------------------------------------------------------

    @PostMapping("/api/policies/{id}/to-prompt")
    public ResponseEntity<ApiResponse<Map<String, String>>> toPrompt(@PathVariable String id) {
        String prompt = aiPolicyService.toDevelopmentPrompt(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("prompt", prompt)));
    }

    // -----------------------------------------------------------------------
    // POST /api/policies/{id}/to-code
    // -----------------------------------------------------------------------

    @PostMapping("/api/policies/{id}/to-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> toCode(@PathVariable String id) {
        String code = aiPolicyService.toZodCode(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("code", code)));
    }

    // -----------------------------------------------------------------------
    // POST /api/policies/{id}/links  (재정의 / 연결)
    // -----------------------------------------------------------------------

    @PostMapping("/api/policies/{id}/links")
    public ResponseEntity<ApiResponse<Void>> linkPolicy(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String componentId = body.get("componentId");
        String overrideContent = body.get("overrideContent");
        String linkType = body.getOrDefault("linkType", "APPLIED");
        if (componentId == null || componentId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("componentId는 필수입니다."));
        }
        policyService.linkToComponent(id, componentId, overrideContent, linkType);
        return ResponseEntity.ok(ApiResponse.ok("정책이 연결되었습니다", null));
    }

    // -----------------------------------------------------------------------
    // GET /api/policies/consistency-check?projectId={id}
    // -----------------------------------------------------------------------

    @GetMapping("/api/policies/consistency-check")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> consistencyCheck(
            @RequestParam String projectId) {
        List<Map<String, Object>> issues = policyService.consistencyCheck(projectId);
        return ResponseEntity.ok(ApiResponse.ok(issues));
    }
}
