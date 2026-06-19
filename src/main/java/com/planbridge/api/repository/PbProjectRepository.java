package com.planbridge.api.repository;

import com.planbridge.api.entity.PbProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbProjectRepository extends JpaRepository<PbProject, String> {
    List<PbProject> findByStatus(String status);
    List<PbProject> findByStatusOrderByCreatedAtDesc(String status);
}
