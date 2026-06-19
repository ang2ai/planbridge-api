package com.planbridge.api.service;

import com.planbridge.api.dto.request.ChangeRequestCreateRequest;
import com.planbridge.api.dto.request.ChangeRequestUpdateRequest;
import com.planbridge.api.dto.response.AnalysisQueueResponse;
import com.planbridge.api.dto.response.ChangeRequestResponse;
import com.planbridge.api.entity.PbAnalysisQueue;
import com.planbridge.api.entity.PbChangeRequest;
import com.planbridge.api.entity.PbComponent;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbAnalysisQueueRepository;
import com.planbridge.api.repository.PbChangeRequestRepository;
import com.planbridge.api.repository.PbComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChangeRequestService {

    private final PbChangeRequestRepository changeRequestRepository;
    private final PbComponentRepository componentRepository;
    private final PbAnalysisQueueRepository analysisQueueRepository;

    public List<ChangeRequestResponse> findByProjectId(String projectId) {
        return changeRequestRepository.findByComponent_Page_Project_ProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(ChangeRequestResponse::from)
                .collect(Collectors.toList());
    }

    public List<ChangeRequestResponse> findAll() {
        return changeRequestRepository.findAll()
                .stream()
                .map(ChangeRequestResponse::from)
                .collect(Collectors.toList());
    }

    public ChangeRequestResponse findById(String requestId) {
        PbChangeRequest cr = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ChangeRequest", requestId));
        return ChangeRequestResponse.from(cr);
    }

    @Transactional
    public ChangeRequestResponse create(ChangeRequestCreateRequest req) {
        // 컴포넌트 조회: componentId가 있으면 실제 DB에서 찾고, 없거나 못 찾으면 null 허용
        PbComponent component = null;
        String componentDescription = req.getComponentDescription();

        if (req.getComponentId() != null && !req.getComponentId().isBlank()) {
            component = componentRepository.findById(req.getComponentId()).orElse(null);
            if (component == null) {
                // UUID로 못 찾은 경우 → 자유 텍스트로 간주하여 componentDescription에 저장
                componentDescription = req.getComponentId();
                log.info("componentId '{}' not found in DB, storing as description", req.getComponentId());
            }
        }

        PbChangeRequest cr = PbChangeRequest.builder()
                .component(component)
                .componentDescription(componentDescription)
                .requestedBy(req.getRequestedBy())
                .title(req.getTitle())
                .description(req.getDescription())
                .currentState(req.getCurrentState())
                .desiredState(req.getDesiredState())
                .priority(req.getPriority() != null ? req.getPriority() : "MEDIUM")
                .build();
        return ChangeRequestResponse.from(changeRequestRepository.save(cr));
    }

    @Transactional
    public ChangeRequestResponse update(String requestId, ChangeRequestUpdateRequest req) {
        PbChangeRequest cr = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ChangeRequest", requestId));

        if (req.getTitle() != null) cr.setTitle(req.getTitle());
        if (req.getDescription() != null) cr.setDescription(req.getDescription());
        if (req.getCurrentState() != null) cr.setCurrentState(req.getCurrentState());
        if (req.getDesiredState() != null) cr.setDesiredState(req.getDesiredState());
        if (req.getPriority() != null) cr.setPriority(req.getPriority());
        if (req.getStatus() != null) cr.setStatus(req.getStatus());
        if (req.getAiAnalysis() != null) cr.setAiAnalysis(req.getAiAnalysis());

        return ChangeRequestResponse.from(changeRequestRepository.save(cr));
    }

    @Transactional
    public AnalysisQueueResponse requestAnalysis(String requestId) {
        PbChangeRequest cr = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ChangeRequest", requestId));

        // component가 null이면 AI 분석 불가 (프로젝트 참조가 없음) — save 전에 먼저 체크
        if (cr.getComponent() == null || cr.getComponent().getPage() == null
                || cr.getComponent().getPage().getProject() == null) {
            throw new com.planbridge.api.exception.BadRequestException(
                    "AI 분석을 실행하려면 Chrome Extension으로 컴포넌트가 연결된 변경 요청이 필요합니다.");
        }

        cr.setStatus("AI_PROCESSING");
        changeRequestRepository.save(cr);

        String payload = String.format(
                "{\"requestId\":\"%s\",\"componentId\":\"%s\",\"title\":\"%s\"}",
                requestId,
                cr.getComponent().getComponentId(),
                cr.getTitle()
        );

        PbAnalysisQueue queue = PbAnalysisQueue.builder()
                .project(cr.getComponent().getPage().getProject())
                .analysisType("CHANGE_REQUEST")
                .requestId(requestId)
                .requestPayload(payload)
                .build();

        return AnalysisQueueResponse.from(analysisQueueRepository.save(queue));
    }

    public AnalysisQueueResponse getAnalysisStatus(String requestId) {
        return analysisQueueRepository.findFirstByRequestIdAndAnalysisTypeOrderByCreatedAtDesc(requestId, "CHANGE_REQUEST")
                .map(AnalysisQueueResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("AnalysisQueue for request", requestId));
    }

    @Transactional
    public ChangeRequestResponse complete(String requestId) {
        PbChangeRequest cr = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ChangeRequest", requestId));
        cr.setStatus("DONE");
        return ChangeRequestResponse.from(changeRequestRepository.save(cr));
    }
}
