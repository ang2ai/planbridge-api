package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_COMPONENT_TEMPLATE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbComponentTemplate {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "TEMPLATE_ID", length = 36)
    private String templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private PbProject project;

    @Column(name = "TEMPLATE_NAME", nullable = false, length = 200)
    private String templateName;

    @Column(name = "COMPONENT_TYPE", nullable = false, length = 30)
    private String componentType;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Lob
    @Column(name = "TEMPLATE_JSON")
    private String templateJson;

    @Column(name = "POLICY_TAGS", length = 500)
    private String policyTags;

    @Column(name = "USAGE_COUNT")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "CREATED_BY", nullable = false, length = 100)
    private String createdBy;

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
