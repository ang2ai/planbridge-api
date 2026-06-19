package com.planbridge.api.dto.request;

import lombok.Data;

@Data
public class WebhookRequest {
    private String projectId;
    private String ref;
    private String commitHash;
    private String commitMessage;
    private Integer filesChanged;
    private String pusher;
}
