package com.planbridge.api.service;

import com.planbridge.api.dto.response.AnalysisQueueResponse;
import com.planbridge.api.entity.PbAnalysisQueue;
import com.planbridge.api.entity.PbProject;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbAnalysisQueueRepository;
import com.planbridge.api.repository.PbProjectRepository;
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
public class AnalysisQueueService {

    private final PbAnalysisQueueRepository analysisQueueRepository;
    private final PbProjectRepository projectRepository;

    public List<AnalysisQueueResponse> findByProjectId(String projectId) {
        return analysisQueueRepository.findByProject_ProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(AnalysisQueueResponse::from)
                .collect(Collectors.toList());
    }

    public List<AnalysisQueueResponse> findQueued() {
        return analysisQueueRepository.findByStatusOrderByCreatedAtAsc("QUEUED")
                .stream()
                .map(AnalysisQueueResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnalysisQueueResponse enqueue(String projectId, String analysisType, String requestId, String payload) {
        PbProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        PbAnalysisQueue queue = PbAnalysisQueue.builder()
                .project(project)
                .analysisType(analysisType)
                .requestId(requestId)
                .requestPayload(payload)
                .build();
        return AnalysisQueueResponse.from(analysisQueueRepository.save(queue));
    }

    @Transactional
    public AnalysisQueueResponse updateStatus(String queueId, String status, String result, String errorMessage) {
        PbAnalysisQueue queue = analysisQueueRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("AnalysisQueue", queueId));
        queue.setStatus(status);
        if (result != null) queue.setResult(result);
        if (errorMessage != null) queue.setErrorMessage(errorMessage);
        return AnalysisQueueResponse.from(analysisQueueRepository.save(queue));
    }
}
