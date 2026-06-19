package com.planbridge.api.controller;

import com.planbridge.api.dto.request.WebhookRequest;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.entity.PbGitSyncLog;
import com.planbridge.api.entity.PbProject;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbGitSyncLogRepository;
import com.planbridge.api.repository.PbProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PbProjectRepository projectRepository;
    private final PbGitSyncLogRepository gitSyncLogRepository;

    /**
     * Git push 웹훅 수신
     * POST /api/webhook/git
     */
    @PostMapping("/git")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, String>>> receiveGitWebhook(
            @RequestBody WebhookRequest req,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-Gitlab-Token", required = false) String gitlabToken) {

        log.info("Git webhook received for project: {}, ref: {}", req.getProjectId(), req.getRef());

        PbProject project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", req.getProjectId()));

        // 브랜치 추출 (refs/heads/main -> main)
        String branch = req.getRef() != null
                ? req.getRef().replaceFirst("^refs/heads/", "")
                : project.getRepoBranch();

        // 대상 브랜치만 처리
        if (!branch.equals(project.getRepoBranch())) {
            log.info("Ignoring webhook for branch: {} (tracking: {})", branch, project.getRepoBranch());
            return ResponseEntity.ok(ApiResponse.ok(Map.of("status", "ignored", "reason", "branch mismatch")));
        }

        // 동기화 상태 업데이트
        project.setSyncStatus("SYNCING");
        projectRepository.save(project);

        // Git 동기화 로그 기록
        PbGitSyncLog syncLog = PbGitSyncLog.builder()
                .project(project)
                .triggerType("WEBHOOK")
                .commitHash(req.getCommitHash())
                .commitMessage(req.getCommitMessage())
                .branch(branch)
                .filesChanged(req.getFilesChanged())
                .status("SUCCESS")
                .build();
        gitSyncLogRepository.save(syncLog);

        // 실제 git fetch는 Git Mirror Daemon이 처리
        project.setSyncStatus("IDLE");
        project.setLastSyncedAt(LocalDateTime.now());
        projectRepository.save(project);

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "status", "accepted",
                "projectId", req.getProjectId(),
                "branch", branch
        )));
    }

    /**
     * GitHub/GitLab 웹훅 형식 수신 (raw)
     * POST /api/webhook/git/github
     */
    @PostMapping("/git/github")
    public ResponseEntity<ApiResponse<Map<String, String>>> receiveGithubWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        String ref = (String) payload.get("ref");
        Object repoObj = payload.get("repository");
        String repoUrl = repoObj instanceof Map ? (String) ((Map<?, ?>) repoObj).get("clone_url") : null;

        log.info("GitHub webhook received: ref={}, repoUrl={}", ref, repoUrl);

        // repoUrl로 프로젝트 찾기
        if (repoUrl != null) {
            projectRepository.findAll().stream()
                    .filter(p -> repoUrl.equals(p.getRepoUrl()))
                    .findFirst()
                    .ifPresent(project -> {
                        project.setSyncStatus("IDLE");
                        project.setLastSyncedAt(LocalDateTime.now());
                        projectRepository.save(project);

                        PbGitSyncLog syncLog = PbGitSyncLog.builder()
                                .project(project)
                                .triggerType("WEBHOOK")
                                .branch(ref != null ? ref.replaceFirst("^refs/heads/", "") : project.getRepoBranch())
                                .status("SUCCESS")
                                .build();
                        gitSyncLogRepository.save(syncLog);
                    });
        }

        return ResponseEntity.ok(ApiResponse.ok(Map.of("status", "accepted")));
    }
}
