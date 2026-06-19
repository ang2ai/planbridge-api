package com.planbridge.api.repository;

import com.planbridge.api.entity.PbPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PbPageRepository extends JpaRepository<PbPage, String> {
    List<PbPage> findByProject_ProjectId(String projectId);
    Optional<PbPage> findByProject_ProjectIdAndRoutePath(String projectId, String routePath);
    List<PbPage> findByProject_ProjectIdAndStatus(String projectId, String status);
}
