package com.planbridge.api.dto.response;

import com.planbridge.api.entity.PbPolicy;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PolicyResponse {
    private String policyId;
    private String projectId;
    private String scope;
    private String pageId;
    private String componentId;
    private String policyType;
    private String policyTitle;
    private String policyContent;
    private String policySchema;
    private String tags;
    private Integer currentVersion;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String linkType;

    public static PolicyResponse from(PbPolicy p) {
        return PolicyResponse.builder()
                .policyId(p.getPolicyId())
                .projectId(p.getProject() != null ? p.getProject().getProjectId() : null)
                .scope(p.getScope())
                .pageId(p.getPage() != null ? p.getPage().getPageId() : null)
                .componentId(p.getComponent() != null ? p.getComponent().getComponentId() : null)
                .policyType(p.getPolicyType())
                .policyTitle(p.getPolicyTitle())
                .policyContent(p.getPolicyContent())
                .policySchema(p.getPolicySchema())
                .tags(p.getTags())
                .currentVersion(p.getCurrentVersion())
                .status(p.getStatus())
                .createdBy(p.getCreatedBy())
                .createdAt(p.getCreatedAt())
                .updatedBy(p.getUpdatedBy())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
