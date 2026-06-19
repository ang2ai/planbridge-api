package com.planbridge.api.dto.response;

import com.planbridge.api.entity.PbScreenPlan;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScreenPlanResponse {

    private String planId;
    private String projectId;
    private String projectName;
    private String planTitle;
    private String routePath;
    private String description;
    private String wireframeJson;
    private String fullSpec;
    private String aiSuggestion;
    private String status;
    private String pageId;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScreenPlanResponse from(PbScreenPlan plan) {
        return ScreenPlanResponse.builder()
                .planId(plan.getPlanId())
                .projectId(plan.getProject() != null ? plan.getProject().getProjectId() : null)
                .projectName(plan.getProject() != null ? plan.getProject().getProjectName() : null)
                .planTitle(plan.getPlanTitle())
                .routePath(plan.getRoutePath())
                .description(plan.getDescription())
                .wireframeJson(plan.getWireframeJson())
                .fullSpec(plan.getFullSpec())
                .aiSuggestion(plan.getAiSuggestion())
                .status(plan.getStatus())
                .pageId(plan.getPage() != null ? plan.getPage().getPageId() : null)
                .createdBy(plan.getCreatedBy())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
