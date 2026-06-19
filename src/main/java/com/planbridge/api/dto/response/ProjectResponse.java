package com.planbridge.api.dto.response;

import com.planbridge.api.entity.PbProject;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {
    private String projectId;
    private String projectName;
    private String projectDesc;
    private String repoUrl;
    private String repoLocalPath;
    private String repoBranch;
    private String baseUrl;
    private String framework;
    private String status;
    private String syncStatus;
    private LocalDateTime lastSyncedAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectResponse from(PbProject p) {
        return ProjectResponse.builder()
                .projectId(p.getProjectId())
                .projectName(p.getProjectName())
                .projectDesc(p.getProjectDesc())
                .repoUrl(p.getRepoUrl())
                .repoLocalPath(p.getRepoLocalPath())
                .repoBranch(p.getRepoBranch())
                .baseUrl(p.getBaseUrl())
                .framework(p.getFramework())
                .status(p.getStatus())
                .syncStatus(p.getSyncStatus())
                .lastSyncedAt(p.getLastSyncedAt())
                .createdBy(p.getCreatedBy())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
