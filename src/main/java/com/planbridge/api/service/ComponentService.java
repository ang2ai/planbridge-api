package com.planbridge.api.service;

import com.planbridge.api.dto.request.ComponentResolveRequest;
import com.planbridge.api.dto.request.ScanDataRequest;
import com.planbridge.api.dto.response.ComponentResponse;
import com.planbridge.api.entity.PbComponent;
import com.planbridge.api.entity.PbComponentSnapshot;
import com.planbridge.api.entity.PbPage;
import com.planbridge.api.entity.PbProject;
import com.planbridge.api.entity.PbScanHistory;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbComponentRepository;
import com.planbridge.api.repository.PbComponentSnapshotRepository;
import com.planbridge.api.repository.PbPageRepository;
import com.planbridge.api.repository.PbProjectRepository;
import com.planbridge.api.repository.PbScanHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ComponentService {

    private final PbComponentRepository componentRepository;
    private final PbPageRepository pageRepository;
    private final PbProjectRepository projectRepository;
    private final PbScanHistoryRepository scanHistoryRepository;
    private final PbComponentSnapshotRepository componentSnapshotRepository;

    public ComponentResponse findById(String componentId) {
        PbComponent component = componentRepository.findById(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("Component", componentId));
        return ComponentResponse.from(component);
    }

    public List<ComponentResponse> findTreeByProjectId(String projectId) {
        List<PbComponent> all = componentRepository.findByProjectId(projectId);
        return buildTree(all);
    }

    public ComponentResponse resolve(ComponentResolveRequest req) {
        List<PbComponent> candidates = componentRepository.findByRouteAndFingerprint(
                req.getProjectId(),
                req.getPageRoute(),
                req.getPbId() != null ? req.getPbId() : "",
                req.getComponentName() != null ? req.getComponentName() : ""
        );

        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException("Component not found for fingerprint");
        }
        return ComponentResponse.from(candidates.get(0));
    }

    @Transactional
    public Map<String, Object> processScan(String projectId, ScanDataRequest req) {
        PbProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        // 페이지 upsert
        PbPage page = pageRepository.findByProject_ProjectIdAndRoutePath(projectId, req.getRoutePath())
                .orElseGet(() -> {
                    PbPage newPage = PbPage.builder()
                            .project(project)
                            .routePath(req.getRoutePath())
                            .pageTitle(req.getPageTitle())
                            .filePath(req.getFilePath())
                            .build();
                    return pageRepository.save(newPage);
                });

        if (req.getPageTitle() != null) page.setPageTitle(req.getPageTitle());
        if (req.getFilePath() != null) page.setFilePath(req.getFilePath());
        pageRepository.save(page);

        // 컴포넌트 upsert (pbId -> component map)
        Map<String, PbComponent> pbIdMap = new HashMap<>();
        int newCount = 0;
        int changedCount = 0;

        if (req.getComponents() != null) {
            // 1차: pbId 없는 것(parent 참조 없는) 먼저 저장
            for (ScanDataRequest.ComponentData cd : req.getComponents()) {
                Optional<PbComponent> existing = componentRepository.findByPage_PageIdAndPbId(page.getPageId(), cd.getPbId());
                PbComponent comp;
                if (existing.isPresent()) {
                    comp = existing.get();
                    comp.setComponentName(cd.getComponentName());
                    comp.setCssSelector(cd.getCssSelector());
                    comp.setCurrentProps(cd.getCurrentProps());
                    comp.setCurrentText(cd.getCurrentText());
                    comp.setReactHierarchy(cd.getReactHierarchy());
                    comp.setTreePath(cd.getTreePath());
                    if (cd.getDepthLevel() != null) comp.setDepthLevel(cd.getDepthLevel());
                    if (cd.getSortOrder() != null) comp.setSortOrder(cd.getSortOrder());
                    changedCount++;
                } else {
                    comp = PbComponent.builder()
                            .page(page)
                            .pbId(cd.getPbId())
                            .componentName(cd.getComponentName())
                            .cssSelector(cd.getCssSelector())
                            .componentType(cd.getComponentType() != null ? cd.getComponentType() : "COMPONENT")
                            .elementTag(cd.getElementTag())
                            .elementRole(cd.getElementRole())
                            .currentProps(cd.getCurrentProps())
                            .currentText(cd.getCurrentText())
                            .reactHierarchy(cd.getReactHierarchy())
                            .treePath(cd.getTreePath())
                            .depthLevel(cd.getDepthLevel() != null ? cd.getDepthLevel() : 0)
                            .sortOrder(cd.getSortOrder() != null ? cd.getSortOrder() : 0)
                            .build();
                    newCount++;
                }
                comp = componentRepository.save(comp);
                pbIdMap.put(cd.getPbId(), comp);
            }

            // 2차: parent 연결
            for (ScanDataRequest.ComponentData cd : req.getComponents()) {
                if (cd.getParentPbId() != null && pbIdMap.containsKey(cd.getParentPbId())) {
                    PbComponent comp = pbIdMap.get(cd.getPbId());
                    comp.setParent(pbIdMap.get(cd.getParentPbId()));
                    componentRepository.save(comp);
                }
            }
        }

        // 스캔 이력 저장
        PbScanHistory scanHistory = PbScanHistory.builder()
                .project(project)
                .page(page)
                .scanType(req.getScanType() != null ? req.getScanType() : "PAGE_SCAN")
                .componentCount(pbIdMap.size())
                .newCount(newCount)
                .changedCount(changedCount)
                .scannedBy(req.getScannedBy())
                .build();
        scanHistoryRepository.save(scanHistory);

        // 각 컴포넌트에 대해 스냅샷 저장
        if (req.getComponents() != null) {
            for (ScanDataRequest.ComponentData cd : req.getComponents()) {
                PbComponent comp = pbIdMap.get(cd.getPbId());
                if (comp == null) continue;

                // propsJson: currentProps + currentText 합산
                String propsJson = buildPropsJson(cd.getCurrentProps(), cd.getCurrentText());

                PbComponentSnapshot snapshot = PbComponentSnapshot.builder()
                        .component(comp)
                        .scanHistory(scanHistory)
                        .propsJson(propsJson)
                        .build();
                componentSnapshotRepository.save(snapshot);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pageId", page.getPageId());
        result.put("scanId", scanHistory.getScanId());
        result.put("totalComponents", pbIdMap.size());
        result.put("newComponents", newCount);
        result.put("changedComponents", changedCount);
        return result;
    }

    private String buildPropsJson(String currentProps, String currentText) {
        if (currentProps != null && !currentProps.isBlank()) {
            // currentProps가 이미 JSON이면 그대로 사용하되 currentText도 포함
            if (currentText != null && !currentText.isBlank()) {
                // currentProps가 JSON 객체 형태라면 text 필드 주입 시도
                String trimmed = currentProps.trim();
                if (trimmed.endsWith("}")) {
                    String escaped = currentText.replace("\\", "\\\\").replace("\"", "\\\"");
                    return trimmed.substring(0, trimmed.length() - 1)
                            + ",\"_text\":\"" + escaped + "\"}";
                }
            }
            return currentProps;
        }
        if (currentText != null && !currentText.isBlank()) {
            String escaped = currentText.replace("\\", "\\\\").replace("\"", "\\\"");
            return "{\"_text\":\"" + escaped + "\"}";
        }
        return null;
    }

    private List<ComponentResponse> buildTree(List<PbComponent> all) {
        Map<String, ComponentResponse> map = new LinkedHashMap<>();
        for (PbComponent c : all) {
            map.put(c.getComponentId(), ComponentResponse.from(c));
        }

        List<ComponentResponse> roots = new ArrayList<>();
        for (PbComponent c : all) {
            ComponentResponse resp = map.get(c.getComponentId());
            if (c.getParent() == null) {
                roots.add(resp);
            } else {
                ComponentResponse parent = map.get(c.getParent().getComponentId());
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                    parent.getChildren().add(resp);
                }
            }
        }
        return roots;
    }
}
