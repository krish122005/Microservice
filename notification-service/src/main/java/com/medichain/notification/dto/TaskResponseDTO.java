package com.medichain.notification.dto;
 
import com.medichain.notification.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Data
@AllArgsConstructor
public class TaskResponseDTO {
 
    private Long id;
    private Long assignedTo;
    private Long relatedEntityId;
    private String relatedEntityType;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
    private LocalDateTime createdAt;
}