package com.planbridge.api.repository;

import com.planbridge.api.entity.PbTodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbTodoItemRepository extends JpaRepository<PbTodoItem, String> {
    List<PbTodoItem> findByChangeRequest_RequestIdOrderBySortOrderAsc(String requestId);
    List<PbTodoItem> findByStatusOrderByCreatedAtDesc(String status);
    List<PbTodoItem> findByStatusNotOrderByCreatedAtDesc(String status);
    List<PbTodoItem> findByCompletedByAndStatusNot(String completedBy, String status);

    @Query("SELECT t FROM PbTodoItem t JOIN t.changeRequest cr JOIN cr.component c JOIN c.page p WHERE p.project.projectId = :projectId ORDER BY t.createdAt DESC")
    List<PbTodoItem> findByProjectId(@Param("projectId") String projectId);

    @Query("SELECT t FROM PbTodoItem t JOIN t.changeRequest cr JOIN cr.component c JOIN c.page p WHERE p.project.projectId = :projectId AND t.status = :status ORDER BY t.createdAt DESC")
    List<PbTodoItem> findByProjectIdAndStatus(@Param("projectId") String projectId, @Param("status") String status);

    @Query("SELECT t FROM PbTodoItem t JOIN t.changeRequest cr JOIN cr.component c JOIN c.page p WHERE p.project.projectId = :projectId AND t.status != 'DONE' ORDER BY t.createdAt DESC")
    List<PbTodoItem> findPendingByProjectId(@Param("projectId") String projectId);
}
