package com.planbridge.api.repository;

import com.planbridge.api.entity.PbValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbValidationRuleRepository extends JpaRepository<PbValidationRule, String> {
    List<PbValidationRule> findByPolicy_PolicyIdOrderBySortOrder(String policyId);
    void deleteByPolicy_PolicyId(String policyId);
}
