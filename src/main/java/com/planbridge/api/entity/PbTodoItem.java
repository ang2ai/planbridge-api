package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_TODO_ITEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbTodoItem {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "TODO_ID", length = 36)
    private String todoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private PbChangeRequest changeRequest;

    @Column(name = "TITLE", nullable = false, length = 500)
    private String title;

    @Lob
    @Column(name = "PROMPT", nullable = false)
    private String prompt;

    @Lob
    @Column(name = "TARGET_FILES")
    private String targetFiles;

    @Column(name = "COMPLEXITY", length = 20)
    @Builder.Default
    private String complexity = "MODERATE";

    @Column(name = "SORT_ORDER")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "DEPENDENCIES", length = 500)
    private String dependencies;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Lob
    @Column(name = "TEST_RESULT")
    private String testResult;

    @Column(name = "COMPLETED_BY", length = 100)
    private String completedBy;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
