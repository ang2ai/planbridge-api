package com.planbridge.api.dto.request;

import lombok.Data;

@Data
public class ChangeRequestUpdateRequest {
    private String title;
    private String description;
    private String currentState;
    private String desiredState;
    private String priority;
    private String status;
    private String aiAnalysis;
}
