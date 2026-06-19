package com.planbridge.api.repository;

import com.planbridge.api.entity.PbComponentSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbComponentSnapshotRepository extends JpaRepository<PbComponentSnapshot, String> {
    List<PbComponentSnapshot> findByComponent_ComponentIdOrderByCreatedAtDesc(String componentId);
}
