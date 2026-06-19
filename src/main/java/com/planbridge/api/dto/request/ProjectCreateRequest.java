package com.planbridge.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCreateRequest {
    @NotBlank(message = "프로젝트명은 필수입니다")
    private String projectName;
    private String projectDesc;
    private String repoUrl;
    private String baseUrl;
    private String framework;
    private String repoBranch;
    @NotBlank(message = "생성자는 필수입니다")
    private String createdBy;
}
