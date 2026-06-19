package com.planbridge.api.dto.response;

import com.planbridge.api.entity.PbChangeRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChangeRequestResponse {
    private String requestId;
    private String componentId;
    private String componentName;
    private String componentDescription;
    private String requestedBy;
    private String title;
    private String description;
    private String currentState;
    private String desiredState;
    private String aiAnalysis;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChangeRequestResponse from(PbChangeRequest cr) {
        return ChangeRequestResponse.builder()
                .requestId(cr.getRequestId())
                .componentId(cr.getComponent() != null ? cr.getComponent().getComponentId() : null)
                .componentName(cr.getComponent() != null ? cr.getComponent().getComponentName() : null)
                .componentDescription(cr.getComponentDescription())
                .requestedBy(cr.getRequestedBy())
                .title(cr.getTitle())
                .description(cr.getDescription())
                .currentState(cr.getCurrentState())
                .desiredState(cr.getDesiredState())
                .aiAnalysis(cr.getAiAnalysis())
                .priority(cr.getPriority())
                .status(cr.getStatus())
                .createdAt(cr.getCreatedAt())
                .updatedAt(cr.getUpdatedAt())
                .build();
    }
}
