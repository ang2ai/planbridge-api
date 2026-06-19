package com.planbridge.api.repository;

import com.planbridge.api.entity.PbAnalysisQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PbAnalysisQueueRepository extends JpaRepository<PbAnalysisQueue, String> {
    List<PbAnalysisQueue> findByStatusOrderByCreatedAtAsc(String status);
    // 같은 변경요청을 여러 번 분석하면 큐 행이 여러 개 쌓이므로, 가장 최근 1건을 반환한다.
    // (단순 findBy...는 행이 2개 이상이면 NonUniqueResultException 발생)
    Optional<PbAnalysisQueue> findFirstByRequestIdAndAnalysisTypeOrderByCreatedAtDesc(String requestId, String analysisType);
    List<PbAnalysisQueue> findByProject_ProjectIdOrderByCreatedAtDesc(String projectId);
}
