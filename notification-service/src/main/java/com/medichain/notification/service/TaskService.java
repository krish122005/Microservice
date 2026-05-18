package com.medichain.notification.service;
 
import com.medichain.notification.dto.TaskRequestDTO;
import com.medichain.notification.dto.TaskResponseDTO;
import com.medichain.notification.entity.Task;
import com.medichain.notification.entity.TaskStatus;
import com.medichain.notification.exception.TaskNotFoundException;
import com.medichain.notification.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDate;
import java.util.List;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
 
    private final TaskRepository taskRepository;
 
    // CREATE
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO dto) {
        Task task = Task.builder()
                .assignedTo(dto.getAssignedTo())
                .relatedEntityId(dto.getRelatedEntityId())
                .relatedEntityType(dto.getRelatedEntityType().toUpperCase())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .status(TaskStatus.PENDING)
                .build();
 
        Task saved = taskRepository.save(task);
        log.info("Task created: id={} assignedTo={}", saved.getId(), saved.getAssignedTo());
        return toDTO(saved);
    }
 
    // GET ALL TASKS FOR A USER
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByUser(Long userId) {
        return taskRepository.findByAssignedToOrderByDueDateAsc(userId)
                .stream().map(this::toDTO).toList();
    }
 
    // GET PENDING TASKS FOR A USER
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getPendingTasksByUser(Long userId) {
        return taskRepository.findByAssignedToAndStatusOrderByDueDateAsc(userId, TaskStatus.PENDING)
                .stream().map(this::toDTO).toList();
    }
 
    // GET ALL TASKS BY STATUS (for admin/auditor)
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByStatus(String status) {
        TaskStatus taskStatus = parseStatus(status);
        return taskRepository.findByStatusOrderByDueDateAsc(taskStatus)
                .stream().map(this::toDTO).toList();
    }
 
    // GET BY ID
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        Task task = findOrThrow(id);
        return toDTO(task);
    }
 
    // UPDATE STATUS
    @Transactional
    public TaskResponseDTO updateTaskStatus(Long id, String status) {
        Task task = findOrThrow(id);
        task.setStatus(parseStatus(status));
        Task updated = taskRepository.save(task);
        log.info("Task status updated: id={} status={}", id, status);
        return toDTO(updated);
    }
 
    // DELETE
    @Transactional
    public void deleteTask(Long id) {
        findOrThrow(id);
        taskRepository.deleteById(id);
        log.info("Task deleted: id={}", id);
    }
 
    // MARK OVERDUE (can be triggered by a scheduler or manually)
    @Transactional
    public int markOverdueTasks() {
        int count = taskRepository.markOverdueTasks(LocalDate.now());
        log.info("Marked {} tasks as OVERDUE", count);
        return count;
    }
 
    // ── helpers ──────────────────────────────────────────────────────────────
 
    private Task findOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
    }
 
    private TaskStatus parseStatus(String raw) {
        try {
            return TaskStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown task status: " + raw +
                    ". Valid values: PENDING, IN_PROGRESS, COMPLETED, OVERDUE, CANCELLED");
        }
    }
 
    private TaskResponseDTO toDTO(Task t) {
        return new TaskResponseDTO(
                t.getId(),
                t.getAssignedTo(),
                t.getRelatedEntityId(),
                t.getRelatedEntityType(),
                t.getDescription(),
                t.getDueDate(),
                t.getStatus(),
                t.getCreatedAt()
        );
    }
}