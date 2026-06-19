package com.planbridge.api.repository;

import com.planbridge.api.entity.PbPlanPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbPlanPolicyRepository extends JpaRepository<PbPlanPolicy, String> {
    List<PbPlanPolicy> findByPlan_PlanId(String planId);
    List<PbPlanPolicy> findByPolicy_PolicyId(String policyId);
}
