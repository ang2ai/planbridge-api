package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "PB_VALIDATION_RULE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbValidationRule {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "RULE_ID", length = 36)
    private String ruleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLICY_ID", nullable = false)
    private PbPolicy policy;

    @Column(name = "RULE_TYPE", nullable = false, length = 50)
    private String ruleType;

    @Column(name = "FIELD_NAME", length = 200)
    private String fieldName;

    @Column(name = "RULE_VALUE", length = 500)
    private String ruleValue;

    @Column(name = "ERROR_MESSAGE", length = 500)
    private String errorMessage;

    @Column(name = "SORT_ORDER")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
