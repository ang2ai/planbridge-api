package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_PROJECT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbProject {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "PROJECT_ID", length = 36)
    private String projectId;

    @Column(name = "PROJECT_NAME", nullable = false, length = 200)
    private String projectName;

    @Column(name = "PROJECT_DESC", length = 4000)
    private String projectDesc;

    @Column(name = "REPO_URL", length = 500)
    private String repoUrl;

    @Column(name = "REPO_LOCAL_PATH", length = 500)
    private String repoLocalPath;

    @Column(name = "REPO_BRANCH", length = 100)
    @Builder.Default
    private String repoBranch = "main";

    @Column(name = "REPO_TOKEN", length = 500)
    private String repoToken;

    @Column(name = "BASE_URL", length = 500)
    private String baseUrl;

    @Column(name = "FRAMEWORK", length = 50)
    @Builder.Default
    private String framework = "NEXTJS";

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "LAST_SYNCED_AT")
    private LocalDateTime lastSyncedAt;

    @Column(name = "SYNC_STATUS", length = 20)
    @Builder.Default
    private String syncStatus = "IDLE";

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
