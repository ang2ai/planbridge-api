package com.planbridge.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PB_COMPONENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PbComponent {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "COMPONENT_ID", length = 36)
    private String componentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAGE_ID", nullable = false)
    private PbPage page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private PbComponent parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<PbComponent> children;

    @Column(name = "PB_ID", nullable = false, length = 500)
    private String pbId;

    @Column(name = "COMPONENT_NAME", nullable = false, length = 200)
    private String componentName;

    @Column(name = "CSS_SELECTOR", length = 2000)
    private String cssSelector;

    @Column(name = "COMPONENT_TYPE", nullable = false, length = 30)
    private String componentType;

    @Column(name = "ELEMENT_TAG", length = 50)
    private String elementTag;

    @Column(name = "ELEMENT_ROLE", length = 100)
    private String elementRole;

    @Lob
    @Column(name = "CURRENT_PROPS")
    private String currentProps;

    @Column(name = "CURRENT_TEXT", length = 4000)
    private String currentText;

    @Lob
    @Column(name = "CURRENT_SPEC")
    private String currentSpec;

    @Column(name = "DEPTH_LEVEL")
    @Builder.Default
    private Integer depthLevel = 0;

    @Column(name = "SORT_ORDER")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "TREE_PATH", length = 2000)
    private String treePath;

    @Lob
    @Column(name = "REACT_HIERARCHY")
    private String reactHierarchy;

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
