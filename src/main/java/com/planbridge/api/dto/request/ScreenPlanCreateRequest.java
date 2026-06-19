package com.planbridge.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScreenPlanCreateRequest {

    @NotBlank(message = "프로젝트ID는 필수입니다")
    private String projectId;

    @NotBlank(message = "기획서 제목은 필수입니다")
    private String planTitle;

    private String routePath;

    private String description;

    @NotBlank(message = "작성자는 필수입니다")
    private String createdBy;
}
