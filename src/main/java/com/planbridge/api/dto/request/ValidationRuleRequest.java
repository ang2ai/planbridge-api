package com.planbridge.api.dto.request;

import lombok.Data;

@Data
public class ValidationRuleRequest {
    private String ruleType;
    private String fieldName;
    private String ruleValue;
    private String errorMessage;
    private Integer sortOrder;
}
