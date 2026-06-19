package com.planbridge.api.repository;

import com.planbridge.api.entity.PbGitSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbGitSyncLogRepository extends JpaRepository<PbGitSyncLog, String> {
    List<PbGitSyncLog> findByProject_ProjectIdOrderBySyncedAtDesc(String projectId);
    List<PbGitSyncLog> findTop10ByProject_ProjectIdOrderBySyncedAtDesc(String projectId);
}
