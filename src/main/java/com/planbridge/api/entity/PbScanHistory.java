package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_SCAN_HISTORY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbScanHistory {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "SCAN_ID", length = 36)
    private String scanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private PbProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAGE_ID")
    private PbPage page;

    @Column(name = "SCAN_TYPE", nullable = false, length = 30)
    private String scanType;

    @Column(name = "COMPONENT_COUNT")
    private Integer componentCount;

    @Column(name = "NEW_COUNT")
    @Builder.Default
    private Integer newCount = 0;

    @Column(name = "CHANGED_COUNT")
    @Builder.Default
    private Integer changedCount = 0;

    @Column(name = "REMOVED_COUNT")
    @Builder.Default
    private Integer removedCount = 0;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(name = "SCANNED_BY", length = 100)
    private String scannedBy;

    @Column(name = "SCANNED_AT")
    private LocalDateTime scannedAt;

    @PrePersist
    public void prePersist() {
        this.scannedAt = LocalDateTime.now();
    }
}
