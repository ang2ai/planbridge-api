package com.planbridge.api.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ScanDataRequest {
    private String routePath;
    private String pageTitle;
    private String filePath;
    private String scanType;
    private String scannedBy;
    private List<ComponentData> components;

    @Data
    public static class ComponentData {
        private String pbId;
        private String componentName;
        private String cssSelector;
        private String componentType;
        private String elementTag;
        private String elementRole;
        private String currentProps;
        private String currentText;
        private String reactHierarchy;
        private String treePath;
        private String parentPbId;
        private Integer depthLevel;
        private Integer sortOrder;
    }
}
