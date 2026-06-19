package com.planbridge.api.service;

import com.planbridge.api.entity.PbPolicy;
import com.planbridge.api.entity.PbTestChecklist;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbPolicyRepository;
import com.planbridge.api.repository.PbTestChecklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TestChecklistService {

    private final PbTestChecklistRepository checklistRepository;
    private final PbPolicyRepository policyRepository;

    public List<PbTestChecklist> findByPolicyId(String policyId) {
        return checklistRepository.findByPolicy_PolicyId(policyId);
    }

    public List<PbTestChecklist> findByPlanId(String planId) {
        return checklistRepository.findByPlan_PlanId(planId);
    }

    /**
     * 정책 제목/내용 기반 규칙 기반 체크리스트 자동 생성.
     * AI 호출 없이 정책 title을 체크리스트 항목으로 변환한다.
     */
    @Transactional
    public List<PbTestChecklist> generateFromPolicy(String policyId, String createdBy) {
        PbPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));

        List<PbTestChecklist> generated = new ArrayList<>();

        // 1. 정책 제목 자체를 하나의 체크리스트 항목으로 생성
        PbTestChecklist titleItem = PbTestChecklist.builder()
                .policy(policy)
                .checklistTitle("[" + policy.getPolicyType() + "] " + policy.getPolicyTitle())
                .checklistContent(policy.getPolicyContent())
                .checkType("MANUAL")
                .status("PENDING")
                .createdBy(createdBy)
                .build();
        generated.add(checklistRepository.save(titleItem));

        // 2. 정책 내용에서 줄바꿈 단위로 추가 항목 추출 (최대 5개)
        if (policy.getPolicyContent() != null && !policy.getPolicyContent().isBlank()) {
            String[] lines = policy.getPolicyContent().split("[\\r\\n]+");
            int count = 0;
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.equals(policy.getPolicyTitle())) {
                    continue;
                }
                if (trimmed.length() < 5) {
                    continue;
                }
                PbTestChecklist lineItem = PbTestChecklist.builder()
                        .policy(policy)
                        .checklistTitle(trimmed.length() > 500 ? trimmed.substring(0, 497) + "..." : trimmed)
                        .checklistContent("정책 '" + policy.getPolicyTitle() + "' 에서 파생된 항목")
                        .checkType("MANUAL")
                        .status("PENDING")
                        .createdBy(createdBy)
                        .build();
                generated.add(checklistRepository.save(lineItem));
                if (++count >= 5) break;
            }
        }

        log.info("Generated {} checklist items from policy {}", generated.size(), policyId);
        return generated;
    }

    @Transactional
    public PbTestChecklist updateStatus(String checklistId, String status, String checkedBy) {
        PbTestChecklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("TestChecklist", checklistId));

        checklist.setStatus(status);
        checklist.setCheckedBy(checkedBy);
        checklist.setCheckedAt(LocalDateTime.now());

        return checklistRepository.save(checklist);
    }
}
