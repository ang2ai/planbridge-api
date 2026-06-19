package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_PAGE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbPage {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "PAGE_ID", length = 36)
    private String pageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private PbProject project;

    @Column(name = "ROUTE_PATH", nullable = false, length = 500)
    private String routePath;

    @Column(name = "PAGE_TITLE", length = 200)
    private String pageTitle;

    @Column(name = "FILE_PATH", length = 500)
    private String filePath;

    @Column(name = "LAYOUT_PATH", length = 500)
    private String layoutPath;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

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
