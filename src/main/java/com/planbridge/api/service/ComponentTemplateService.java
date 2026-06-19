package com.planbridge.api.service;

import com.planbridge.api.dto.request.ComponentTemplateCreateRequest;
import com.planbridge.api.dto.request.ComponentTemplateUpdateRequest;
import com.planbridge.api.dto.response.ComponentTemplateResponse;
import com.planbridge.api.entity.PbComponentTemplate;
import com.planbridge.api.entity.PbProject;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbComponentTemplateRepository;
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
public class ComponentTemplateService {

    private final PbComponentTemplateRepository templateRepository;
    private final PbProjectRepository projectRepository;

    public List<ComponentTemplateResponse> findByProjectId(String projectId) {
        return templateRepository.findByProject_ProjectIdAndStatusNot(projectId, "DELETED")
                .stream()
                .map(ComponentTemplateResponse::from)
                .collect(Collectors.toList());
    }

    public ComponentTemplateResponse findById(String templateId) {
        PbComponentTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("ComponentTemplate", templateId));
        return ComponentTemplateResponse.from(template);
    }

    @Transactional
    public ComponentTemplateResponse create(ComponentTemplateCreateRequest req) {
        PbProject project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", req.getProjectId()));

        PbComponentTemplate template = PbComponentTemplate.builder()
                .project(project)
                .templateName(req.getTemplateName())
                .componentType(req.getComponentType())
                .description(req.getDescription())
                .templateJson(req.getTemplateJson())
                .policyTags(req.getPolicyTags())
                .createdBy(req.getCreatedBy())
                .build();

        return ComponentTemplateResponse.from(templateRepository.save(template));
    }

    @Transactional
    public ComponentTemplateResponse update(String templateId, ComponentTemplateUpdateRequest req) {
        PbComponentTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("ComponentTemplate", templateId));

        if (req.getTemplateName() != null) template.setTemplateName(req.getTemplateName());
        if (req.getComponentType() != null) template.setComponentType(req.getComponentType());
        if (req.getDescription() != null) template.setDescription(req.getDescription());
        if (req.getTemplateJson() != null) template.setTemplateJson(req.getTemplateJson());
        if (req.getPolicyTags() != null) template.setPolicyTags(req.getPolicyTags());
        if (req.getStatus() != null) template.setStatus(req.getStatus());

        return ComponentTemplateResponse.from(templateRepository.save(template));
    }

    @Transactional
    public void delete(String templateId) {
        PbComponentTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("ComponentTemplate", templateId));
        template.setStatus("DELETED");
        templateRepository.save(template);
    }

    @Transactional
    public ComponentTemplateResponse incrementUsage(String templateId) {
        PbComponentTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("ComponentTemplate", templateId));
        template.setUsageCount(template.getUsageCount() + 1);
        return ComponentTemplateResponse.from(templateRepository.save(template));
    }
}
