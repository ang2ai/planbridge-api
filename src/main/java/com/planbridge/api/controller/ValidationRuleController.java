package com.planbridge.api.controller;

import com.planbridge.api.dto.request.ValidationRuleRequest;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.dto.response.ValidationRuleResponse;
import com.planbridge.api.service.ValidationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ValidationRuleController {

    private final ValidationRuleService validationRuleService;

    @GetMapping("/api/policies/{policyId}/rules")
    public ResponseEntity<ApiResponse<List<ValidationRuleResponse>>> getRules(
            @PathVariable String policyId) {
        return ResponseEntity.ok(ApiResponse.ok(validationRuleService.findByPolicyId(policyId)));
    }

    @PutMapping("/api/policies/{policyId}/rules")
    public ResponseEntity<ApiResponse<List<ValidationRuleResponse>>> saveRules(
            @PathVariable String policyId,
            @RequestBody List<ValidationRuleRequest> rules) {
        return ResponseEntity.ok(ApiResponse.ok(
                "검증 룰이 저장되었습니다",
                validationRuleService.saveRules(policyId, rules)));
    }
}
