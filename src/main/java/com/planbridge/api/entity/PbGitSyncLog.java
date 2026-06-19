package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_GIT_SYNC_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbGitSyncLog {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "SYNC_LOG_ID", length = 36)
    private String syncLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private PbProject project;

    @Column(name = "TRIGGER_TYPE", nullable = false, length = 20)
    private String triggerType;

    @Column(name = "COMMIT_HASH", length = 40)
    private String commitHash;

    @Column(name = "COMMIT_MESSAGE", length = 1000)
    private String commitMessage;

    @Column(name = "BRANCH", length = 100)
    private String branch;

    @Column(name = "FILES_CHANGED")
    private Integer filesChanged;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(name = "ERROR_MESSAGE", length = 4000)
    private String errorMessage;

    @Column(name = "SYNCED_AT")
    private LocalDateTime syncedAt;

    @PrePersist
    public void prePersist() {
        this.syncedAt = LocalDateTime.now();
    }
}
