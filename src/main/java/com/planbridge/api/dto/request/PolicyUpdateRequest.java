package com.planbridge.api.dto.request;

import lombok.Data;

@Data
public class PolicyUpdateRequest {
    private String policyTitle;
    private String policyContent;
    private String policySchema;
    private String tags;
    private String changeReason;
    private String updatedBy;
    private String status;
}
