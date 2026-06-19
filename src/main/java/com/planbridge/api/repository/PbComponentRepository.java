package com.planbridge.api.repository;

import com.planbridge.api.entity.PbComponent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PbComponentRepository extends JpaRepository<PbComponent, String> {
    List<PbComponent> findByPage_PageIdOrderByDepthLevelAscSortOrderAsc(String pageId);
    List<PbComponent> findByPage_PageIdAndParentIsNull(String pageId);
    Optional<PbComponent> findByPage_PageIdAndPbId(String pageId, String pbId);

    @EntityGraph(attributePaths = {"page", "page.project"})
    Optional<PbComponent> findWithPageAndProjectByComponentId(String componentId);

    @Query("SELECT c FROM PbComponent c JOIN c.page p WHERE p.project.projectId = :projectId AND c.status = 'ACTIVE' ORDER BY c.depthLevel, c.sortOrder")
    List<PbComponent> findByProjectId(@Param("projectId") String projectId);

    @Query("SELECT c FROM PbComponent c JOIN c.page p WHERE p.routePath = :routePath AND p.project.projectId = :projectId AND (c.pbId = :pbId OR c.componentName = :componentName) ORDER BY CASE WHEN c.pbId = :pbId THEN 1 ELSE 2 END")
    List<PbComponent> findByRouteAndFingerprint(
            @Param("projectId") String projectId,
            @Param("routePath") String routePath,
            @Param("pbId") String pbId,
            @Param("componentName") String componentName
    );
}
