package com.planbridge.api.dto.request;

import lombok.Data;

@Data
public class ScreenPlanUpdateRequest {

    private String planTitle;

    private String routePath;

    private String description;

    private String wireframeJson;

    private String fullSpec;

    private String status;

    private String updatedBy;
}
