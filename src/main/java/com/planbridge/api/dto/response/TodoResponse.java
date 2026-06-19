package com.planbridge.api.dto.response;

import com.planbridge.api.entity.PbTodoItem;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TodoResponse {
    private String todoId;
    private String requestId;
    private String title;
    private String prompt;
    private String targetFiles;
    private String complexity;
    private Integer sortOrder;
    private String dependencies;
    private String status;
    private String testResult;
    private String completedBy;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public static TodoResponse from(PbTodoItem t) {
        return TodoResponse.builder()
                .todoId(t.getTodoId())
                .requestId(t.getChangeRequest() != null ? t.getChangeRequest().getRequestId() : null)
                .title(t.getTitle())
                .prompt(t.getPrompt())
                .targetFiles(t.getTargetFiles())
                .complexity(t.getComplexity())
                .sortOrder(t.getSortOrder())
                .dependencies(t.getDependencies())
                .status(t.getStatus())
                .testResult(t.getTestResult())
                .completedBy(t.getCompletedBy())
                .completedAt(t.getCompletedAt())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
