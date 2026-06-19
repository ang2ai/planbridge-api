package com.planbridge.api.dto.response;

import com.planbridge.api.entity.PbComponent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ComponentResponse {
    private String componentId;
    private String pageId;
    private String projectId;
    private String parentId;
    private String pbId;
    private String componentName;
    private String cssSelector;
    private String componentType;
    private String elementTag;
    private String elementRole;
    private String currentProps;
    private String currentText;
    private String currentSpec;
    private Integer depthLevel;
    private Integer sortOrder;
    private String treePath;
    private String reactHierarchy;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ComponentResponse> children;

    public static ComponentResponse from(PbComponent c) {
        return ComponentResponse.builder()
                .componentId(c.getComponentId())
                .pageId(c.getPage() != null ? c.getPage().getPageId() : null)
                .projectId(c.getPage() != null && c.getPage().getProject() != null
                        ? c.getPage().getProject().getProjectId() : null)
                .parentId(c.getParent() != null ? c.getParent().getComponentId() : null)
                .pbId(c.getPbId())
                .componentName(c.getComponentName())
                .cssSelector(c.getCssSelector())
                .componentType(c.getComponentType())
                .elementTag(c.getElementTag())
                .elementRole(c.getElementRole())
                .currentProps(c.getCurrentProps())
                .currentText(c.getCurrentText())
                .currentSpec(c.getCurrentSpec())
                .depthLevel(c.getDepthLevel())
                .sortOrder(c.getSortOrder())
                .treePath(c.getTreePath())
                .reactHierarchy(c.getReactHierarchy())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
