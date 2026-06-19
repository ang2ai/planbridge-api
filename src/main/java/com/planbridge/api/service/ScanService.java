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

@Service
@RequiredArgsConstructor
@Slf4j
public class ScanService {

    private final PbProjectRepository projectRepository;
    private final PbAnalysisQueueRepository analysisQueueRepository;

    @Transactional
    public AnalysisQueueResponse requestFullScan(String projectId) {
        PbProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        String payload = String.format("{\"projectId\":\"%s\",\"scanType\":\"FULL_SCAN\"}", projectId);

        PbAnalysisQueue queue = PbAnalysisQueue.builder()
                .project(project)
                .analysisType("FULL_SCAN")
                .requestId(projectId)
                .requestPayload(payload)
                .build();

        log.info("Full scan requested for project: {}", projectId);
        return AnalysisQueueResponse.from(analysisQueueRepository.save(queue));
    }
}
