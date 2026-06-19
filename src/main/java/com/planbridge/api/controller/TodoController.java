package com.planbridge.api.controller;

import com.planbridge.api.dto.request.TodoUpdateRequest;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.dto.response.TodoResponse;
import com.planbridge.api.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TodoResponse>>> list(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.findAll(projectId, status)));
    }

    /**
     * GET /api/todos/pending
     * Returns all non-DONE todos for a dashboard widget.
     *
     * Query params (all optional):
     *   projectId — filter by project
     *   assignee  — filter by assigned username (stored in completedBy for in-flight items)
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<TodoResponse>>> pending(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String assignee) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.findPending(projectId, assignee)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> update(
            @PathVariable String id,
            @RequestBody TodoUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("TODO가 수정되었습니다", todoService.update(id, req)));
    }

    /**
     * GET /api/todos/{id}/prompt
     * Returns the rich prompt for a TODO. If the stored prompt is empty it is
     * built on-the-fly from component spec, policy schemas, and target files.
     */
    @GetMapping("/{id}/prompt")
    public ResponseEntity<ApiResponse<Map<String, String>>> prompt(@PathVariable String id) {
        String prompt = todoService.getPrompt(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("todoId", id, "prompt", prompt)));
    }

    @PostMapping("/export")
    public ResponseEntity<ApiResponse<Map<String, Object>>> export(@RequestBody Map<String, List<String>> req) {
        List<String> ids = req.get("todoIds");
        List<TodoResponse> todos = todoService.exportByIds(ids);
        String markdown = todoService.buildExportMarkdown(todos);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("markdown", markdown);
        result.put("todos", todos);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
