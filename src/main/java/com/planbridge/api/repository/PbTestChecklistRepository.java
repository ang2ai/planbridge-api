package com.planbridge.api.repository;

import com.planbridge.api.entity.PbTestChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbTestChecklistRepository extends JpaRepository<PbTestChecklist, String> {
    List<PbTestChecklist> findByPolicy_PolicyId(String policyId);
    List<PbTestChecklist> findByPlan_PlanId(String planId);
}
