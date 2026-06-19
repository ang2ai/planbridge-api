package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_ANALYSIS_QUEUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbAnalysisQueue {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "QUEUE_ID", length = 36)
    private String queueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private PbProject project;

    @Column(name = "ANALYSIS_TYPE", nullable = false, length = 30)
    private String analysisType;

    @Column(name = "REQUEST_ID", length = 36)
    private String requestId;

    @Lob
    @Column(name = "REQUEST_PAYLOAD", nullable = false)
    private String requestPayload;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "QUEUED";

    @Column(name = "WORKER_ID", length = 100)
    private String workerId;

    @Lob
    @Column(name = "RESULT")
    private String result;

    @Column(name = "ERROR_MESSAGE", length = 4000)
    private String errorMessage;

    @Column(name = "RETRY_COUNT")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "MAX_RETRIES")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
