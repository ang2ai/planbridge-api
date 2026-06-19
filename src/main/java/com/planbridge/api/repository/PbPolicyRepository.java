package com.planbridge.api.repository;

import com.planbridge.api.entity.PbPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbPolicyRepository extends JpaRepository<PbPolicy, String> {
    List<PbPolicy> findByProject_ProjectIdAndStatus(String projectId, String status);
    List<PbPolicy> findByComponent_ComponentIdAndStatus(String componentId, String status);
    List<PbPolicy> findByPage_PageIdAndStatus(String pageId, String status);
    List<PbPolicy> findByScopeAndProject_ProjectIdAndStatus(String scope, String projectId, String status);

    @Query("SELECT p FROM PbPolicy p WHERE p.project.projectId = :projectId AND p.status = 'ACTIVE' AND (LOWER(p.policyTitle) LIKE :q OR LOWER(p.tags) LIKE :q)")
    List<PbPolicy> searchByKeyword(@Param("projectId") String projectId, @Param("q") String q);

    @Query("SELECT p FROM PbPolicy p WHERE p.project.projectId = :projectId AND p.status = 'ACTIVE' AND (LOWER(p.policyTitle) LIKE :q OR LOWER(p.tags) LIKE :q)")
    List<PbPolicy> searchGlobal(@Param("q") String q, @Param("projectId") String projectId);

    // projectId 없이 전체 검색 (대시보드 통계용)
    @Query("SELECT p FROM PbPolicy p WHERE p.status = 'ACTIVE' AND (LOWER(p.policyTitle) LIKE :q OR LOWER(p.tags) LIKE :q OR :q = '%%')")
    List<PbPolicy> searchAll(@Param("q") String q);
}
