package com.planbridge.api.dto.request;

import lombok.Data;

@Data
public class ComponentResolveRequest {
    private String projectId;
    private String pbId;
    private String componentName;
    private String cssSelector;
    private String pageRoute;
}
