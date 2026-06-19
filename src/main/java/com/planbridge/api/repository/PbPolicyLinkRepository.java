package com.planbridge.api.repository;

import com.planbridge.api.entity.PbPolicyLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PbPolicyLinkRepository extends JpaRepository<PbPolicyLink, String> {
    List<PbPolicyLink> findByComponent_ComponentId(String componentId);
    List<PbPolicyLink> findByPolicy_PolicyId(String policyId);
    Optional<PbPolicyLink> findByPolicy_PolicyIdAndComponent_ComponentId(String policyId, String componentId);
    void deleteByPolicy_PolicyIdAndComponent_ComponentId(String policyId, String componentId);
}
