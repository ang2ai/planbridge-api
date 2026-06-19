package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_CHANGE_REQUEST")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbChangeRequest {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "REQUEST_ID", length = 36)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPONENT_ID", nullable = true)
    private PbComponent component;

    // Chrome Extension 없이 수동 입력 시 자유 텍스트 컴포넌트 설명 저장
    @Column(name = "COMPONENT_DESCRIPTION", length = 500)
    private String componentDescription;

    @Column(name = "REQUESTED_BY", nullable = false, length = 100)
    private String requestedBy;

    @Column(name = "TITLE", nullable = false, length = 500)
    private String title;

    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Lob
    @Column(name = "CURRENT_STATE")
    private String currentState;

    @Lob
    @Column(name = "DESIRED_STATE")
    private String desiredState;

    @Lob
    @Column(name = "AI_ANALYSIS")
    private String aiAnalysis;

    @Column(name = "PRIORITY", length = 20)
    @Builder.Default
    private String priority = "MEDIUM";

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
