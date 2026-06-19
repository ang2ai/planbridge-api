package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_POLICY_VERSION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbPolicyVersion {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "VERSION_ID", length = 36)
    private String versionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLICY_ID", nullable = false)
    private PbPolicy policy;

    @Column(name = "VERSION_NO", nullable = false)
    private Integer versionNo;

    @Lob
    @Column(name = "POLICY_CONTENT", nullable = false)
    private String policyContent;

    @Lob
    @Column(name = "POLICY_SCHEMA")
    private String policySchema;

    @Column(name = "CHANGE_REASON", length = 2000)
    private String changeReason;

    @Column(name = "REQUEST_ID", length = 36)
    private String requestId;

    @Column(name = "CREATED_BY", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
