package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_TEST_CHECKLIST")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbTestChecklist {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "CHECKLIST_ID", length = 36)
    private String checklistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLICY_ID")
    private PbPolicy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLAN_ID")
    private PbScreenPlan plan;

    @Column(name = "CHECKLIST_TITLE", nullable = false, length = 500)
    private String checklistTitle;

    @Lob
    @Column(name = "CHECKLIST_CONTENT")
    private String checklistContent;

    @Column(name = "CHECK_TYPE", length = 50)
    @Builder.Default
    private String checkType = "MANUAL";

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "CHECKED_BY", length = 100)
    private String checkedBy;

    @Column(name = "CHECKED_AT")
    private LocalDateTime checkedAt;

    @Column(name = "CREATED_BY", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
