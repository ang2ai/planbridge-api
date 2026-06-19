package com.planbridge.api.repository;

import com.planbridge.api.entity.PbChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbChangeRequestRepository extends JpaRepository<PbChangeRequest, String> {
    List<PbChangeRequest> findByComponent_ComponentIdOrderByCreatedAtDesc(String componentId);
    List<PbChangeRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<PbChangeRequest> findByComponent_Page_Project_ProjectIdOrderByCreatedAtDesc(String projectId);
}
