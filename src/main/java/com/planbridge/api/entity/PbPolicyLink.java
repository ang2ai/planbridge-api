package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_POLICY_LINK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbPolicyLink {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "LINK_ID", length = 36)
    private String linkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLICY_ID", nullable = false)
    private PbPolicy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPONENT_ID", nullable = false)
    private PbComponent component;

    @Column(name = "LINK_TYPE", length = 30)
    @Builder.Default
    private String linkType = "APPLIED";

    @Lob
    @Column(name = "OVERRIDE_CONTENT")
    private String overrideContent;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
