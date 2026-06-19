package com.planbridge.api.service;

import com.planbridge.api.dto.request.ProjectCreateRequest;
import com.planbridge.api.dto.request.ProjectUpdateRequest;
import com.planbridge.api.dto.response.ProjectResponse;
import com.planbridge.api.entity.PbGitSyncLog;
import com.planbridge.api.entity.PbProject;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbGitSyncLogRepository;
import com.planbridge.api.repository.PbProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProjectService {

    private final PbProjectRepository projectRepository;
    private final PbGitSyncLogRepository gitSyncLogRepository;

    @Value("${planbridge.git-mirror.url:http://localhost:3001}")
    private String gitMirrorUrl;

    public List<ProjectResponse> findAll() {
        return projectRepository.findByStatusOrderByCreatedAtDesc("ACTIVE")
                .stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    public ProjectResponse findById(String projectId) {
        PbProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse create(ProjectCreateRequest req) {
        PbProject project = PbProject.builder()
                .projectName(req.getProjectName())
                .projectDesc(req.getProjectDesc())
                .repoUrl(req.getRepoUrl())
                .baseUrl(req.getBaseUrl())
                .framework(req.getFramework() != null ? req.getFramework() : "NEXTJS")
                .repoBranch(req.getRepoBranch() != null ? req.getRepoBranch() : "main")
                .createdBy(req.getCreatedBy())
                .build();
        ProjectResponse response = ProjectResponse.from(projectRepository.save(project));

        // git-mirror에 비동기 알림 (실패해도 프로젝트 생성은 성공)
        if (req.getRepoUrl() != null && !req.getRepoUrl().isBlank()) {
            notifyGitMirror(response.getProjectId());
        }

        return response;
    }

    private void notifyGitMirror(String projectId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(gitMirrorUrl + "/api/sync/" + projectId))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("Content-Type", "application/json")
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(res -> log.info("git-mirror 알림 완료: projectId={}, status={}", projectId, res.statusCode()))
                    .exceptionally(ex -> {
                        log.warn("git-mirror 알림 실패 (무시): projectId={}, error={}", projectId, ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.warn("git-mirror 알림 전송 중 오류 (무시): projectId={}, error={}", projectId, e.getMessage());
        }
    }

    @Transactional
    public ProjectResponse update(String projectId, ProjectUpdateRequest req) {
        PbProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        if (req.getProjectName() != null) project.setProjectName(req.getProjectName());
        if (req.getProjectDesc() != null) project.setProjectDesc(req.getProjectDesc());
        if (req.getRepoUrl() != null) project.setRepoUrl(req.getRepoUrl());
        if (req.getBaseUrl() != null) project.setBaseUrl(req.getBaseUrl());
        if (req.getFramework() != null) project.setFramework(req.getFramework());
        if (req.getRepoBranch() != null) project.setRepoBranch(req.getRepoBranch());
        if (req.getStatus() != null) project.setStatus(req.getStatus());

        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse triggerSync(String projectId) {
        PbProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        project.setSyncStatus("SYNCING");
        projectRepository.save(project);

        log.info("Manual git sync triggered for project: {}", projectId);

        // 실제 git fetch 로직은 별도 워커에서 처리
        // 여기서는 상태만 업데이트하고 로그 기록
        project.setSyncStatus("IDLE");
        project.setLastSyncedAt(LocalDateTime.now());
        projectRepository.save(project);

        PbGitSyncLog syncLog = PbGitSyncLog.builder()
                .project(project)
                .triggerType("MANUAL")
                .branch(project.getRepoBranch())
                .status("SUCCESS")
                .build();
        gitSyncLogRepository.save(syncLog);

        return ProjectResponse.from(project);
    }
}
