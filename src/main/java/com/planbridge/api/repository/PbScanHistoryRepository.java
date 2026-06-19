package com.planbridge.api.repository;

import com.planbridge.api.entity.PbScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbScanHistoryRepository extends JpaRepository<PbScanHistory, String> {
    List<PbScanHistory> findByProject_ProjectIdOrderByScannedAtDesc(String projectId);
    List<PbScanHistory> findByProject_ProjectIdAndPage_PageIdOrderByScannedAtDesc(String projectId, String pageId);
}
