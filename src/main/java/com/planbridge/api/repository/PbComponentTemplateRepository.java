package com.planbridge.api.repository;

import com.planbridge.api.entity.PbComponentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbComponentTemplateRepository extends JpaRepository<PbComponentTemplate, String> {
    List<PbComponentTemplate> findByProject_ProjectIdAndStatusNot(String projectId, String status);
}
