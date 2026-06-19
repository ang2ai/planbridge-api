package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_COMPONENT_SNAPSHOT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbComponentSnapshot {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "SNAPSHOT_ID", length = 36)
    private String snapshotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPONENT_ID", nullable = false)
    private PbComponent component;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCAN_HISTORY_ID")
    private PbScanHistory scanHistory;

    @Lob
    @Column(name = "PROPS_JSON")
    private String propsJson;

    @Lob
    @Column(name = "RECT_JSON")
    private String rectJson;

    @Column(name = "SCREENSHOT_URL", length = 500)
    private String screenshotUrl;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
