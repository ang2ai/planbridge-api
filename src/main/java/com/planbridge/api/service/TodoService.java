package com.planbridge.api.service;

import com.planbridge.api.dto.request.TodoUpdateRequest;
import com.planbridge.api.dto.response.TodoResponse;
import com.planbridge.api.entity.PbChangeRequest;
import com.planbridge.api.entity.PbComponent;
import com.planbridge.api.entity.PbPolicy;
import com.planbridge.api.entity.PbPolicyLink;
import com.planbridge.api.entity.PbTodoItem;
import com.planbridge.api.exception.ResourceNotFoundException;
import com.planbridge.api.repository.PbChangeRequestRepository;
import com.planbridge.api.repository.PbPolicyLinkRepository;
import com.planbridge.api.repository.PbPolicyRepository;
import com.planbridge.api.repository.PbTodoItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TodoService {

    private final PbTodoItemRepository todoItemRepository;
    private final PbChangeRequestRepository changeRequestRepository;
    private final PbPolicyLinkRepository policyLinkRepository;
    private final PbPolicyRepository policyRepository;

    public List<TodoResponse> findAll(String projectId, String status) {
        if (projectId != null && status != null) {
            return todoItemRepository.findByProjectIdAndStatus(projectId, status)
                    .stream().map(TodoResponse::from).collect(Collectors.toList());
        } else if (projectId != null) {
            return todoItemRepository.findByProjectId(projectId)
                    .stream().map(TodoResponse::from).collect(Collectors.toList());
        } else if (status != null) {
            return todoItemRepository.findByStatusOrderByCreatedAtDesc(status)
                    .stream().map(TodoResponse::from).collect(Collectors.toList());
        }
        return todoItemRepository.findAll()
                .stream().map(TodoResponse::from).collect(Collectors.toList());
    }

    public TodoResponse findById(String todoId) {
        PbTodoItem todo = todoItemRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", todoId));
        return TodoResponse.from(todo);
    }

    /**
     * Returns todos whose status is not DONE (i.e. pending/in-progress).
     * Optional filters: projectId, assignee (completedBy field used as assignee).
     */
    public List<TodoResponse> findPending(String projectId, String assignee) {
        if (projectId != null) {
            return todoItemRepository.findPendingByProjectId(projectId)
                    .stream().map(TodoResponse::from).collect(Collectors.toList());
        }
        if (assignee != null) {
            // completedBy doubles as the assigned-to field for in-flight items
            return todoItemRepository.findByCompletedByAndStatusNot(assignee, "DONE")
                    .stream().map(TodoResponse::from).collect(Collectors.toList());
        }
        return todoItemRepository.findByStatusNotOrderByCreatedAtDesc("DONE")
                .stream().map(TodoResponse::from).collect(Collectors.toList());
    }

    /**
     * Returns the rich prompt for a TODO, rebuilding it from related context
     * if the stored prompt is blank.
     */
    public String getPrompt(String todoId) {
        PbTodoItem todo = todoItemRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", todoId));

        // If already has a rich prompt, return it directly
        if (todo.getPrompt() != null && !todo.getPrompt().isBlank()) {
            return todo.getPrompt();
        }
        // Otherwise generate on-the-fly from related context
        return buildRichPrompt(todo);
    }

    @Transactional
    public TodoResponse update(String todoId, TodoUpdateRequest req) {
        PbTodoItem todo = todoItemRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", todoId));

        if (req.getStatus() != null) {
            todo.setStatus(req.getStatus());
            if ("DONE".equals(req.getStatus())) {
                todo.setCompletedAt(LocalDateTime.now());
                if (req.getCompletedBy() != null) todo.setCompletedBy(req.getCompletedBy());
            }
        }
        if (req.getTestResult() != null) todo.setTestResult(req.getTestResult());

        PbTodoItem saved = todoItemRepository.save(todo);

        // Auto-complete the parent ChangeRequest when all its TODOs are DONE
        if ("DONE".equals(req.getStatus())) {
            autoCompleteChangeRequestIfAllDone(saved);
        }

        return TodoResponse.from(saved);
    }

    public List<TodoResponse> exportByIds(List<String> todoIds) {
        return todoIds.stream()
                .map(id -> todoItemRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Todo", id)))
                .map(TodoResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 완료된 TODO 목록을 Markdown 형식으로 변환합니다.
     */
    public String buildExportMarkdown(List<TodoResponse> todos) {
        StringBuilder md = new StringBuilder();
        md.append("# 완료된 개발 TODO 목록\n\n");
        md.append("총 ").append(todos.size()).append("건\n\n");
        md.append("---\n\n");

        int i = 1;
        for (TodoResponse t : todos) {
            md.append("## ").append(i++).append(". ").append(t.getTitle()).append("\n\n");
            md.append("- **복잡도**: ").append(t.getComplexity()).append("\n");
            if (t.getTargetFiles() != null && !t.getTargetFiles().isBlank()) {
                md.append("- **대상 파일**: `").append(t.getTargetFiles()).append("`\n");
            }
            if (t.getCompletedBy() != null) {
                md.append("- **완료자**: ").append(t.getCompletedBy()).append("\n");
            }
            if (t.getCompletedAt() != null) {
                md.append("- **완료 일시**: ").append(t.getCompletedAt()).append("\n");
            }
            md.append("\n");
            if (t.getPrompt() != null && !t.getPrompt().isBlank()) {
                md.append("<details><summary>프롬프트 내용 보기</summary>\n\n");
                md.append("```\n").append(t.getPrompt()).append("\n```\n\n");
                md.append("</details>\n\n");
            }
            md.append("---\n\n");
        }
        return md.toString();
    }

    /**
     * Returns all TODOs that belong to a specific ChangeRequest.
     */
    public List<TodoResponse> findByRequestId(String requestId) {
        // Verify the change request exists
        changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ChangeRequest", requestId));
        return todoItemRepository.findByChangeRequest_RequestIdOrderBySortOrderAsc(requestId)
                .stream().map(TodoResponse::from).collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Checks whether all TODOs for the same ChangeRequest are DONE.
     * If so, sets the ChangeRequest status to DONE automatically.
     */
    private void autoCompleteChangeRequestIfAllDone(PbTodoItem completedTodo) {
        PbChangeRequest cr = completedTodo.getChangeRequest();
        if (cr == null) return;

        String requestId = cr.getRequestId();
        List<PbTodoItem> allTodos =
                todoItemRepository.findByChangeRequest_RequestIdOrderBySortOrderAsc(requestId);

        if (allTodos.isEmpty()) return;

        boolean allDone = allTodos.stream().allMatch(t -> "DONE".equals(t.getStatus()));
        if (allDone) {
            changeRequestRepository.findById(requestId).ifPresent(changeRequest -> {
                changeRequest.setStatus("DONE");
                changeRequest.setUpdatedAt(LocalDateTime.now());
                changeRequestRepository.save(changeRequest);
                log.info("ChangeRequest {} auto-completed because all {} TODOs are DONE",
                        requestId, allTodos.size());
            });
        }
    }

    /**
     * Builds a rich, structured prompt for a TODO item by pulling in:
     * - Step-by-step task title / description
     * - Target file paths (from targetFiles field and component cssSelector)
     * - Current component spec (currentSpec)
     * - Related policy JSON schemas
     */
    public String buildRichPrompt(PbTodoItem todo) {
        PbChangeRequest cr = todo.getChangeRequest();
        if (cr == null) return todo.getPrompt() != null ? todo.getPrompt() : "";

        PbComponent component = cr.getComponent();
        StringBuilder sb = new StringBuilder();

        // ── Header ────────────────────────────────────────────────────────────
        sb.append("# Task: ").append(todo.getTitle()).append("\n\n");

        // ── Change request context ─────────────────────────────────────────────
        sb.append("## Change Request\n");
        sb.append("- **Title**: ").append(cr.getTitle()).append("\n");
        sb.append("- **Description**: ").append(cr.getDescription()).append("\n");
        if (cr.getCurrentState() != null && !cr.getCurrentState().isBlank()) {
            sb.append("- **Current State**: ").append(cr.getCurrentState()).append("\n");
        }
        if (cr.getDesiredState() != null && !cr.getDesiredState().isBlank()) {
            sb.append("- **Desired State**: ").append(cr.getDesiredState()).append("\n");
        }
        sb.append("\n");

        // ── Target files ──────────────────────────────────────────────────────
        sb.append("## Target Files\n");
        if (todo.getTargetFiles() != null && !todo.getTargetFiles().isBlank()) {
            sb.append(todo.getTargetFiles()).append("\n");
        }
        if (component != null) {
            if (component.getCssSelector() != null && !component.getCssSelector().isBlank()) {
                sb.append("- CSS Selector: `").append(component.getCssSelector()).append("`\n");
            }
            if (component.getTreePath() != null && !component.getTreePath().isBlank()) {
                sb.append("- Component Tree Path: `").append(component.getTreePath()).append("`\n");
            }
        }
        sb.append("\n");

        // ── Component current spec ─────────────────────────────────────────────
        if (component != null && component.getCurrentSpec() != null
                && !component.getCurrentSpec().isBlank()) {
            sb.append("## Current Component Spec\n");
            sb.append("```json\n").append(component.getCurrentSpec()).append("\n```\n\n");
        }

        // ── Related policies ──────────────────────────────────────────────────
        if (component != null) {
            List<PbPolicyLink> links =
                    policyLinkRepository.findByComponent_ComponentId(component.getComponentId());
            if (!links.isEmpty()) {
                sb.append("## Related Policies\n");
                for (PbPolicyLink link : links) {
                    PbPolicy policy = link.getPolicy();
                    sb.append("### ").append(policy.getPolicyTitle())
                      .append(" (").append(policy.getPolicyType()).append(")\n");
                    sb.append(policy.getPolicyContent()).append("\n");
                    if (policy.getPolicySchema() != null && !policy.getPolicySchema().isBlank()) {
                        sb.append("**Schema:**\n```json\n")
                          .append(policy.getPolicySchema())
                          .append("\n```\n");
                    }
                    sb.append("\n");
                }
            }
        }

        // ── Step-by-step instructions ─────────────────────────────────────────
        sb.append("## Step-by-Step Instructions\n");
        sb.append("1. Review the current component spec and the related policies above.\n");
        sb.append("2. Locate the target files listed in the **Target Files** section.\n");
        sb.append("3. Apply the changes described in the **Change Request** section.\n");
        sb.append("4. Ensure the implementation satisfies every policy constraint "
                + "(especially the JSON schema if provided).\n");
        sb.append("5. Write or update unit/integration tests to cover the changed behaviour.\n");
        sb.append("6. Mark this TODO as DONE once the changes have been verified.\n");

        return sb.toString();
    }
}
