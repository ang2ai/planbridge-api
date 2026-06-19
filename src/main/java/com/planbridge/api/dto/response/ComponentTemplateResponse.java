package com.planbridge.api.dto.response;

import com.planbridge.api.entity.PbComponentTemplate;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ComponentTemplateResponse {
    private String templateId;
    private String projectId;
    private String templateName;
    private String componentType;
    private String description;
    private String templateJson;
    private String policyTags;
    private Integer usageCount;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ComponentTemplateResponse from(PbComponentTemplate t) {
        return ComponentTemplateResponse.builder()
                .templateId(t.getTemplateId())
                .projectId(t.getProject() != null ? t.getProject().getProjectId() : null)
                .templateName(t.getTemplateName())
                .componentType(t.getComponentType())
                .description(t.getDescription())
                .templateJson(t.getTemplateJson())
                .policyTags(t.getPolicyTags())
                .usageCount(t.getUsageCount())
                .status(t.getStatus())
                .createdBy(t.getCreatedBy())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
