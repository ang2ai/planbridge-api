package com.planbridge.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PolicyImpactResponse {
    private String scope;
    private Long affectedCount;
    private List<AffectedComponent> affectedComponents;

    @Data
    @Builder
    public static class AffectedComponent {
        private String componentId;
        private String componentName;
        private String pagePath;
    }
}
