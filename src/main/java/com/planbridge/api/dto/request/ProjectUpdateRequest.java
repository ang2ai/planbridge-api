package com.planbridge.api.dto.request;

import lombok.Data;

@Data
public class ProjectUpdateRequest {
    private String projectName;
    private String projectDesc;
    private String repoUrl;
    private String baseUrl;
    private String framework;
    private String repoBranch;
    private String status;
}
