package com.planbridge.api.dto.response;

import com.planbridge.api.entity.PbAnalysisQueue;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalysisQueueResponse {
    private String queueId;
    private String projectId;
    private String analysisType;
    private String requestId;
    private String status;
    private String workerId;
    private String result;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static AnalysisQueueResponse from(PbAnalysisQueue q) {
        return AnalysisQueueResponse.builder()
                .queueId(q.getQueueId())
                .projectId(q.getProject() != null ? q.getProject().getProjectId() : null)
                .analysisType(q.getAnalysisType())
                .requestId(q.getRequestId())
                .status(q.getStatus())
                .workerId(q.getWorkerId())
                .result(q.getResult())
                .errorMessage(q.getErrorMessage())
                .retryCount(q.getRetryCount())
                .createdAt(q.getCreatedAt())
                .startedAt(q.getStartedAt())
                .completedAt(q.getCompletedAt())
                .build();
    }
}
