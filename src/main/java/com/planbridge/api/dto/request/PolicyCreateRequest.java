package com.planbridge.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PolicyCreateRequest {
    @NotBlank(message = "프로젝트ID는 필수입니다")
    private String projectId;
    @NotBlank(message = "적용범위는 필수입니다")
    private String scope;
    private String pageId;
    private String componentId;
    @NotBlank(message = "정책유형은 필수입니다")
    private String policyType;
    @NotBlank(message = "정책제목은 필수입니다")
    private String policyTitle;
    @NotBlank(message = "정책내용은 필수입니다")
    private String policyContent;
    private String policySchema;
    private String tags;
    @NotBlank(message = "생성자는 필수입니다")
    private String createdBy;
}
