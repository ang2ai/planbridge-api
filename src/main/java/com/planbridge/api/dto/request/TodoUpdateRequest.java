package com.planbridge.api.dto.request;

import lombok.Data;

@Data
public class TodoUpdateRequest {
    private String status;
    private String completedBy;
    private String testResult;
}
