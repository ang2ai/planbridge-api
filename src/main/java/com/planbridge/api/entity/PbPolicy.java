package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_POLICY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbPolicy {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "POLICY_ID", length = 36)
    private String policyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private PbProject project;

    @Column(name = "SCOPE", nullable = false, length = 20)
    private String scope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAGE_ID")
    private PbPage page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPONENT_ID")
    private PbComponent component;

    @Column(name = "POLICY_TYPE", nullable = false, length = 30)
    private String policyType;

    @Column(name = "POLICY_TITLE", nullable = false, length = 500)
    private String policyTitle;

    @Lob
    @Column(name = "POLICY_CONTENT", nullable = false)
    private String policyContent;

    @Lob
    @Column(name = "POLICY_SCHEMA")
    private String policySchema;

    @Column(name = "TAGS", length = 2000)
    private String tags;

    @Column(name = "CURRENT_VERSION")
    @Builder.Default
    private Integer currentVersion = 1;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "CREATED_BY", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_BY", length = 100)
    private String updatedBy;

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
