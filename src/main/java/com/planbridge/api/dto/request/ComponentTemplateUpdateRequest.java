package com.planbridge.api.dto.request;

import lombok.Data;

@Data
public class ComponentTemplateUpdateRequest {
    private String templateName;
    private String componentType;
    private String description;
    private String templateJson;
    private String policyTags;
    private String status;
}
