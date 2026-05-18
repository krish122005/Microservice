package com.medichain.notification.controller;
 
import com.medichain.notification.dto.TaskRequestDTO;
import com.medichain.notification.dto.TaskResponseDTO;
import com.medichain.notification.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
 
    private final TaskService taskService;
 
    // CREATE TASK
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(
            @Valid @RequestBody TaskRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(dto));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTasksByUser(
            @PathVariable Long userId,
            @RequestHeader("X-Auth-UserId") String tokenUserId,
            @RequestHeader("X-Auth-Role") String authRole) {
 
        if (!"ADMIN".equals(authRole) &&
                !tokenUserId.equals(String.valueOf(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied — you can only view your own tasks");
        }
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }
 
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<?> getPendingTasks(
            @PathVariable Long userId,
            @RequestHeader("X-Auth-UserId") String tokenUserId,
            @RequestHeader("X-Auth-Role") String authRole) {
 
        if (!"ADMIN".equals(authRole) &&
                !tokenUserId.equals(String.valueOf(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied — you can only view your own tasks");
        }
        return ResponseEntity.ok(taskService.getPendingTasksByUser(userId));
    }
 
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponseDTO>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }
 
    // GET TASK BY ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }
 
    // UPDATE TASK STATUS
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
    }
 
    // DELETE TASK
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully");
    }
 
    // MARK OVERDUE TASKS (trigger manually or via scheduler)
    @PutMapping("/mark-overdue")
    public ResponseEntity<Map<String, Integer>> markOverdue() {
        int count = taskService.markOverdueTasks();
        return ResponseEntity.ok(Map.of("markedOverdue", count));
    }
}