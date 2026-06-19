package com.planbridge.api.repository;

import com.planbridge.api.entity.PbScreenPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PbScreenPlanRepository extends JpaRepository<PbScreenPlan, String> {

    List<PbScreenPlan> findByProject_ProjectIdAndStatusNotOrderByCreatedAtDesc(String projectId, String status);

    List<PbScreenPlan> findByProject_ProjectIdOrderByCreatedAtDesc(String projectId);

    List<PbScreenPlan> findAllByStatusNotOrderByCreatedAtDesc(String status);
}
