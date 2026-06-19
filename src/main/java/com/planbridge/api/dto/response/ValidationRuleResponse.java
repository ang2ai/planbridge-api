package com.planbridge.api.dto.response;

import com.planbridge.api.entity.PbValidationRule;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ValidationRuleResponse {
    private String ruleId;
    private String policyId;
    private String ruleType;
    private String fieldName;
    private String ruleValue;
    private String errorMessage;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    public static ValidationRuleResponse from(PbValidationRule r) {
        return ValidationRuleResponse.builder()
                .ruleId(r.getRuleId())
                .policyId(r.getPolicy() != null ? r.getPolicy().getPolicyId() : null)
                .ruleType(r.getRuleType())
                .fieldName(r.getFieldName())
                .ruleValue(r.getRuleValue())
                .errorMessage(r.getErrorMessage())
                .sortOrder(r.getSortOrder())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
