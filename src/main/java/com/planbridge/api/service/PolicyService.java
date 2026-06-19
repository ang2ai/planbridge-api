package com.planbridge.api.service;

import com.planbridge.api.dto.request.PolicyCreateRequest;
import com.planbridge.api.dto.request.PolicyUpdateRequest;
import com.planbridge.api.dto.response.PolicyImpactResponse;
import com.planbridge.api.dto.response.PolicyResponse;
import com.planbridge.api.entity.*;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class PolicyService {

    private final PbPolicyRepository policyRepository;
    private final PbPolicyVersionRepository policyVersionRepository;
    private final PbPolicyLinkRepository policyLinkRepository;
    private final PbProjectRepository projectRepository;
    private final PbPageRepository pageRepository;
    private final PbComponentRepository componentRepository;
    private final ValidationRuleService validationRuleService;

    public PolicyService(PbPolicyRepository policyRepository,
                         PbPolicyVersionRepository policyVersionRepository,
                         PbPolicyLinkRepository policyLinkRepository,
                         PbProjectRepository projectRepository,
                         PbPageRepository pageRepository,
                         PbComponentRepository componentRepository,
                         @Lazy ValidationRuleService validationRuleService) {
        this.policyRepository = policyRepository;
        this.policyVersionRepository = policyVersionRepository;
        this.policyLinkRepository = policyLinkRepository;
        this.projectRepository = projectRepository;
        this.pageRepository = pageRepository;
        this.componentRepository = componentRepository;
        this.validationRuleService = validationRuleService;
    }

    public List<PolicyResponse> findByComponent(String componentId) {
        // 중복 제거용 policyId 추적 Set
        Set<String> seen = new LinkedHashSet<>();
        List<PolicyResponse> result = new ArrayList<>();

        // 1. 직접 적용된 정책 (SCOPE=COMPONENT, COMPONENT_ID=componentId)
        List<PbPolicy> direct = policyRepository.findByComponent_ComponentIdAndStatus(componentId, "ACTIVE");
        direct.forEach(p -> {
            if (seen.add(p.getPolicyId())) {
                PolicyResponse resp = PolicyResponse.from(p);
                resp.setLinkType("APPLIED");
                result.add(resp);
            }
        });

        // 2. PB_POLICY_LINK 통해 연결된 정책
        List<PbPolicyLink> links = policyLinkRepository.findByComponent_ComponentId(componentId);
        links.forEach(l -> {
            String pid = l.getPolicy().getPolicyId();
            if (seen.add(pid)) {
                PolicyResponse resp = PolicyResponse.from(l.getPolicy());
                resp.setLinkType(l.getLinkType());
                result.add(resp);
            }
        });

        // 3. 계층 상속: PAGE / GLOBAL 정책 추가 (page, project eager 로딩)
        PbComponent component = componentRepository.findWithPageAndProjectByComponentId(componentId).orElse(null);
        if (component != null && component.getPage() != null) {
            PbPage page = component.getPage();
            String pageId = page.getPageId();

            // 3a. PAGE 범위 정책
            List<PbPolicy> pagePolicies = policyRepository.findByPage_PageIdAndStatus(pageId, "ACTIVE");
            pagePolicies.forEach(p -> {
                if (seen.add(p.getPolicyId())) {
                    PolicyResponse resp = PolicyResponse.from(p);
                    resp.setLinkType("INHERITED_PAGE");
                    result.add(resp);
                }
            });

            // 3b. GLOBAL 범위 정책 (프로젝트 전체)
            if (page.getProject() != null) {
                String projectId = page.getProject().getProjectId();
                List<PbPolicy> globalPolicies = policyRepository
                        .findByScopeAndProject_ProjectIdAndStatus("GLOBAL", projectId, "ACTIVE");
                globalPolicies.forEach(p -> {
                    if (seen.add(p.getPolicyId())) {
                        PolicyResponse resp = PolicyResponse.from(p);
                        resp.setLinkType("INHERITED_GLOBAL");
                        result.add(resp);
                    }
                });
            }
        }

        return result;
    }

    public PolicyResponse findById(String policyId) {
        PbPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));
        return PolicyResponse.from(policy);
    }

    public List<PolicyResponse> search(String projectId, String q) {
        String keyword = "%" + (q == null ? "" : q.toLowerCase()) + "%";
        List<PbPolicy> policies;
        if (projectId == null || projectId.isBlank()) {
            // projectId 없으면 전체 검색
            policies = policyRepository.searchAll(keyword);
        } else {
            policies = policyRepository.searchByKeyword(projectId, keyword);
        }
        return policies.stream().map(PolicyResponse::from).collect(Collectors.toList());
    }

    public List<PolicyResponse> findHistoryByPolicy(String policyId) {
        return policyVersionRepository.findByPolicy_PolicyIdOrderByVersionNoDesc(policyId)
                .stream()
                .map(v -> PolicyResponse.builder()
                        .policyId(policyId)
                        .currentVersion(v.getVersionNo())
                        .policyContent(v.getPolicyContent())
                        .policySchema(v.getPolicySchema())
                        .createdBy(v.getCreatedBy())
                        .createdAt(v.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public PolicyResponse create(PolicyCreateRequest req) {
        PbProject project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", req.getProjectId()));

        PbPolicy.PbPolicyBuilder builder = PbPolicy.builder()
                .project(project)
                .scope(req.getScope())
                .policyType(req.getPolicyType())
                .policyTitle(req.getPolicyTitle())
                .policyContent(req.getPolicyContent())
                .policySchema(req.getPolicySchema())
                .tags(req.getTags())
                .createdBy(req.getCreatedBy());

        if (req.getPageId() != null) {
            PbPage page = pageRepository.findById(req.getPageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Page", req.getPageId()));
            builder.page(page);
        }

        if (req.getComponentId() != null) {
            PbComponent component = componentRepository.findById(req.getComponentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Component", req.getComponentId()));
            builder.component(component);
        }

        PbPolicy policy = policyRepository.save(builder.build());

        // 컴포넌트 연결이 있으면 PB_POLICY_LINK도 생성
        if (req.getComponentId() != null) {
            PbComponent component = componentRepository.findById(req.getComponentId()).get();
            PbPolicyLink link = PbPolicyLink.builder()
                    .policy(policy)
                    .component(component)
                    .linkType("APPLIED")
                    .build();
            policyLinkRepository.save(link);
        }

        // VALIDATION 타입이면 정책 내용에서 룰 자동 파싱
        if ("VALIDATION".equals(req.getPolicyType()) && req.getPolicyContent() != null) {
            validationRuleService.parseFromPolicyContent(policy.getPolicyId(), req.getPolicyContent());
        }

        return PolicyResponse.from(policy);
    }

    @Transactional
    public PolicyResponse update(String policyId, PolicyUpdateRequest req) {
        PbPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));

        // 버전 이력 저장
        PbPolicyVersion version = PbPolicyVersion.builder()
                .policy(policy)
                .versionNo(policy.getCurrentVersion())
                .policyContent(policy.getPolicyContent())
                .policySchema(policy.getPolicySchema())
                .changeReason(req.getChangeReason())
                .createdBy(req.getUpdatedBy() != null ? req.getUpdatedBy() : "system")
                .build();
        policyVersionRepository.save(version);

        // 정책 업데이트
        boolean contentChanged = req.getPolicyContent() != null
                && !req.getPolicyContent().equals(policy.getPolicyContent());

        if (req.getPolicyTitle() != null) policy.setPolicyTitle(req.getPolicyTitle());
        if (req.getPolicyContent() != null) policy.setPolicyContent(req.getPolicyContent());
        if (req.getPolicySchema() != null) policy.setPolicySchema(req.getPolicySchema());
        if (req.getTags() != null) policy.setTags(req.getTags());
        if (req.getUpdatedBy() != null) policy.setUpdatedBy(req.getUpdatedBy());
        if (req.getStatus() != null) policy.setStatus(req.getStatus());
        policy.setCurrentVersion(policy.getCurrentVersion() + 1);

        PbPolicy saved = policyRepository.save(policy);

        // VALIDATION 타입이고 content가 변경됐으면 룰 재파싱
        if ("VALIDATION".equals(saved.getPolicyType()) && contentChanged) {
            validationRuleService.parseFromPolicyContent(saved.getPolicyId(), saved.getPolicyContent());
        }

        return PolicyResponse.from(saved);
    }

    // -----------------------------------------------------------------------
    // Policy Link (Override)
    // -----------------------------------------------------------------------

    @Transactional
    public void linkToComponent(String policyId, String componentId, String overrideContent, String linkType) {
        PbPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));
        PbComponent component = componentRepository.findById(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("Component", componentId));

        // 이미 존재하면 덮어쓰기
        policyLinkRepository.findByPolicy_PolicyIdAndComponent_ComponentId(policyId, componentId)
                .ifPresent(existing -> policyLinkRepository.deleteByPolicy_PolicyIdAndComponent_ComponentId(policyId, componentId));

        PbPolicyLink link = PbPolicyLink.builder()
                .policy(policy)
                .component(component)
                .linkType(linkType != null ? linkType : "APPLIED")
                .overrideContent(overrideContent)
                .build();
        policyLinkRepository.save(link);
    }

    @Transactional
    public void delete(String policyId) {
        PbPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));
        policy.setStatus("DELETED");
        policyRepository.save(policy);
    }

    // -----------------------------------------------------------------------
    // Consistency check
    // -----------------------------------------------------------------------

    public List<Map<String, Object>> consistencyCheck(String projectId) {
        List<PbPolicy> policies = policyRepository.findByProject_ProjectIdAndStatus(projectId, "ACTIVE");
        List<Map<String, Object>> issues = new ArrayList<>();

        // 1. DUPLICATE_TITLE — policies whose title shares the first 20 characters
        Map<String, List<PbPolicy>> byPrefix = new java.util.LinkedHashMap<>();
        for (PbPolicy p : policies) {
            String title = p.getPolicyTitle() != null ? p.getPolicyTitle() : "";
            String prefix = title.length() > 20 ? title.substring(0, 20) : title;
            byPrefix.computeIfAbsent(prefix, k -> new ArrayList<>()).add(p);
        }
        for (Map.Entry<String, List<PbPolicy>> entry : byPrefix.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<String> ids = entry.getValue().stream()
                        .map(PbPolicy::getPolicyId)
                        .collect(Collectors.toList());
                Map<String, Object> issue = new java.util.LinkedHashMap<>();
                issue.put("type", "DUPLICATE_TITLE");
                issue.put("policyIds", ids);
                issue.put("description", "정책 제목의 앞 20자가 동일합니다: \"" + entry.getKey() + "\"");
                issues.add(issue);
            }
        }

        // 2. CONFLICTING_SCOPE — same policyType + scope + same component
        // Group by policyType + scope + componentId (non-null)
        Map<String, List<PbPolicy>> byTypeScope = new java.util.LinkedHashMap<>();
        for (PbPolicy p : policies) {
            String componentId = p.getComponent() != null ? p.getComponent().getComponentId() : null;
            if (componentId == null) continue; // Only flag component-level conflicts
            String key = p.getPolicyType() + "||" + p.getScope() + "||" + componentId;
            byTypeScope.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }
        for (Map.Entry<String, List<PbPolicy>> entry : byTypeScope.entrySet()) {
            if (entry.getValue().size() > 1) {
                String[] parts = entry.getKey().split("\\|\\|", 3);
                List<String> ids = entry.getValue().stream()
                        .map(PbPolicy::getPolicyId)
                        .collect(Collectors.toList());
                Map<String, Object> issue = new java.util.LinkedHashMap<>();
                issue.put("type", "CONFLICTING_SCOPE");
                issue.put("policyIds", ids);
                issue.put("description", "동일한 컴포넌트에 같은 유형/범위의 정책이 중복 적용되었습니다"
                        + " (type=" + parts[0] + ", scope=" + parts[1] + ", componentId=" + parts[2] + ")");
                issues.add(issue);
            }
        }

        return issues;
    }

    public PolicyImpactResponse getImpact(String policyId) {
        PbPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));

        String scope = policy.getScope();
        List<PolicyImpactResponse.AffectedComponent> affected = new ArrayList<>();

        if ("GLOBAL".equals(scope)) {
            // 프로젝트 전체 활성 컴포넌트
            String projectId = policy.getProject().getProjectId();
            List<PbComponent> components = componentRepository.findByProjectId(projectId);
            components.stream()
                    .filter(c -> "ACTIVE".equals(c.getStatus()))
                    .forEach(c -> affected.add(PolicyImpactResponse.AffectedComponent.builder()
                            .componentId(c.getComponentId())
                            .componentName(c.getComponentName())
                            .pagePath(c.getPage() != null ? c.getPage().getRoutePath() : null)
                            .build()));

        } else if ("PAGE".equals(scope)) {
            // 해당 페이지의 컴포넌트
            if (policy.getPage() != null) {
                String pageId = policy.getPage().getPageId();
                List<PbComponent> components =
                        componentRepository.findByPage_PageIdOrderByDepthLevelAscSortOrderAsc(pageId);
                components.stream()
                        .filter(c -> "ACTIVE".equals(c.getStatus()))
                        .forEach(c -> affected.add(PolicyImpactResponse.AffectedComponent.builder()
                                .componentId(c.getComponentId())
                                .componentName(c.getComponentName())
                                .pagePath(c.getPage() != null ? c.getPage().getRoutePath() : null)
                                .build()));
            }

        } else {
            // COMPONENT scope: PB_POLICY_LINK + 직접 연결 컴포넌트
            Set<String> seen = new LinkedHashSet<>();

            // 직접 연결된 컴포넌트
            if (policy.getComponent() != null) {
                PbComponent c = policy.getComponent();
                if (seen.add(c.getComponentId())) {
                    affected.add(PolicyImpactResponse.AffectedComponent.builder()
                            .componentId(c.getComponentId())
                            .componentName(c.getComponentName())
                            .pagePath(c.getPage() != null ? c.getPage().getRoutePath() : null)
                            .build());
                }
            }

            // POLICY_LINK를 통해 연결된 컴포넌트
            List<PbPolicyLink> links = policyLinkRepository.findByPolicy_PolicyId(policyId);
            links.forEach(link -> {
                PbComponent c = link.getComponent();
                if (c != null && seen.add(c.getComponentId())) {
                    affected.add(PolicyImpactResponse.AffectedComponent.builder()
                            .componentId(c.getComponentId())
                            .componentName(c.getComponentName())
                            .pagePath(c.getPage() != null ? c.getPage().getRoutePath() : null)
                            .build());
                }
            });
        }

        return PolicyImpactResponse.builder()
                .scope(scope)
                .affectedCount((long) affected.size())
                .affectedComponents(affected)
                .build();
    }
}
