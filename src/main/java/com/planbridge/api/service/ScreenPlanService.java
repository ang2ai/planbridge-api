package com.planbridge.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbridge.api.dto.request.ScreenPlanCreateRequest;
import com.planbridge.api.dto.request.ScreenPlanUpdateRequest;
import com.planbridge.api.dto.response.AnalysisQueueResponse;
import com.planbridge.api.dto.response.ScreenPlanResponse;
import com.planbridge.api.entity.PbPlanPolicy;
import com.planbridge.api.entity.PbProject;
import com.planbridge.api.entity.PbScreenPlan;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbPlanPolicyRepository;
import com.planbridge.api.repository.PbProjectRepository;
import com.planbridge.api.repository.PbScreenPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScreenPlanService {

    private final PbScreenPlanRepository screenPlanRepository;
    private final PbProjectRepository projectRepository;
    private final AnalysisQueueService analysisQueueService;
    private final ObjectMapper objectMapper;
    private final PbPlanPolicyRepository planPolicyRepository;

    public List<ScreenPlanResponse> findByProjectId(String projectId) {
        return screenPlanRepository.findByProject_ProjectIdAndStatusNotOrderByCreatedAtDesc(projectId, "DELETED")
                .stream()
                .map(ScreenPlanResponse::from)
                .collect(Collectors.toList());
    }

    public List<ScreenPlanResponse> findAll() {
        return screenPlanRepository.findAllByStatusNotOrderByCreatedAtDesc("DELETED")
                .stream()
                .map(ScreenPlanResponse::from)
                .collect(Collectors.toList());
    }

    public ScreenPlanResponse findById(String planId) {
        PbScreenPlan plan = screenPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("ScreenPlan", planId));
        return ScreenPlanResponse.from(plan);
    }

    @Transactional
    public ScreenPlanResponse create(ScreenPlanCreateRequest req) {
        PbProject project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", req.getProjectId()));

        PbScreenPlan plan = PbScreenPlan.builder()
                .project(project)
                .planTitle(req.getPlanTitle())
                .routePath(req.getRoutePath())
                .description(req.getDescription())
                .createdBy(req.getCreatedBy())
                .build();

        return ScreenPlanResponse.from(screenPlanRepository.save(plan));
    }

    @Transactional
    public ScreenPlanResponse update(String planId, ScreenPlanUpdateRequest req) {
        PbScreenPlan plan = screenPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("ScreenPlan", planId));

        if (req.getPlanTitle() != null) plan.setPlanTitle(req.getPlanTitle());
        if (req.getRoutePath() != null) plan.setRoutePath(req.getRoutePath());
        if (req.getDescription() != null) plan.setDescription(req.getDescription());
        if (req.getWireframeJson() != null) plan.setWireframeJson(req.getWireframeJson());
        if (req.getFullSpec() != null) plan.setFullSpec(req.getFullSpec());
        if (req.getStatus() != null) plan.setStatus(req.getStatus());

        return ScreenPlanResponse.from(screenPlanRepository.save(plan));
    }

    @Transactional
    public AnalysisQueueResponse requestAnalysis(String planId, String analysisType) {
        PbScreenPlan plan = screenPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("ScreenPlan", planId));

        // worker analyzer(newPlanAnalyzer)가 기대하는 키(planDescription, screenTitle)에 맞춰 구성.
        // HashMap 사용: description이 null이어도 안전(Map.of는 null 불가).
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("planId", planId);
        payloadMap.put("projectId", plan.getProject().getProjectId());
        payloadMap.put("planTitle", plan.getPlanTitle());
        payloadMap.put("screenTitle", plan.getPlanTitle());
        payloadMap.put("planDescription", plan.getDescription() != null ? plan.getDescription() : "");
        payloadMap.put("routePath", plan.getRoutePath() != null ? plan.getRoutePath() : "");
        payloadMap.put("analysisType", analysisType);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            log.error("AI 분석 페이로드 생성 실패", e);
            payload = String.format(
                    "{\"planId\":\"%s\",\"projectId\":\"%s\",\"planTitle\":\"%s\",\"analysisType\":\"%s\"}",
                    planId, plan.getProject().getProjectId(), plan.getPlanTitle(), analysisType
            );
        }

        return analysisQueueService.enqueue(
                plan.getProject().getProjectId(),
                analysisType,
                planId,
                payload
        );
    }

    // 기획서 분석(NEW_PLAN/CONFLICT_CHECK/GENERATE_SPEC) 진행 상태 조회 (프론트 폴링용).
    public AnalysisQueueResponse getAnalysisStatus(String planId, String analysisType) {
        return analysisQueueService.getLatestStatus(planId, analysisType);
    }

    @Transactional
    public void delete(String planId) {
        PbScreenPlan plan = screenPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("ScreenPlan", planId));
        plan.setStatus("DELETED");
        screenPlanRepository.save(plan);
    }

    // -----------------------------------------------------------------------
    // Export plan as Markdown
    // -----------------------------------------------------------------------

    public String exportAsMarkdown(String planId) {
        PbScreenPlan plan = screenPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("ScreenPlan", planId));

        List<PbPlanPolicy> planPolicies = planPolicyRepository.findByPlan_PlanId(planId);

        StringBuilder md = new StringBuilder();
        md.append("# ").append(nvl(plan.getPlanTitle())).append("\n\n");

        if (plan.getRoutePath() != null && !plan.getRoutePath().isBlank()) {
            md.append("**경로**: `").append(plan.getRoutePath()).append("`\n\n");
        }

        md.append("**상태**: ").append(nvl(plan.getStatus())).append("\n\n");

        if (plan.getDescription() != null && !plan.getDescription().isBlank()) {
            md.append("## 설명\n\n").append(plan.getDescription()).append("\n\n");
        }

        if (plan.getFullSpec() != null && !plan.getFullSpec().isBlank()) {
            md.append("## 전체 기획서\n\n").append(plan.getFullSpec()).append("\n\n");
        }

        if (plan.getAiSuggestion() != null && !plan.getAiSuggestion().isBlank()) {
            md.append("## AI 제안\n\n").append(plan.getAiSuggestion()).append("\n\n");
        }

        if (!planPolicies.isEmpty()) {
            md.append("## 연결된 정책\n\n");
            md.append("| # | 정책명 | 유형 | 범위 |\n");
            md.append("|---|--------|------|------|\n");
            int i = 1;
            for (PbPlanPolicy pp : planPolicies) {
                if (pp.getPolicy() != null) {
                    md.append("| ").append(i++).append(" | ")
                      .append(nvl(pp.getPolicy().getPolicyTitle())).append(" | ")
                      .append(nvl(pp.getPolicy().getPolicyType())).append(" | ")
                      .append(nvl(pp.getPolicy().getScope())).append(" |\n");
                }
            }
            md.append("\n");
        }

        return md.toString();
    }

    private static String nvl(String value) {
        return value != null ? value : "";
    }
}
