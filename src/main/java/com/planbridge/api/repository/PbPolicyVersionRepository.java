package com.planbridge.api.repository;

import com.planbridge.api.entity.PbPolicyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbPolicyVersionRepository extends JpaRepository<PbPolicyVersion, String> {
    List<PbPolicyVersion> findByPolicy_PolicyIdOrderByVersionNoDesc(String policyId);
    int countByPolicy_PolicyId(String policyId);
}
