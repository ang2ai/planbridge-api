package com.planbridge.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeRequestCreateRequest {
    // Chrome Extension 사용 시 실제 UUID, 수동 입력 시 null 허용
    private String componentId;
    // Chrome Extension 없이 수동 입력 시 자유 텍스트 (예: "상품 관리 > 할인율 입력 필드")
    private String componentDescription;
    @NotBlank(message = "요청자는 필수입니다")
    private String requestedBy;
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    @NotBlank(message = "설명은 필수입니다")
    private String description;
    private String currentState;
    private String desiredState;
    private String priority;
}
