package com.medichain.notification.dto;
 
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
import java.time.LocalDate;
 
@Data
public class TaskRequestDTO {
 
    @NotNull(message = "assignedTo is required")
    private Long assignedTo;
 
    private Long relatedEntityId;
 
    @NotBlank(message = "relatedEntityType is required")
    private String relatedEntityType;  // REQUEST / DELIVERY / INVOICE / INVENTORY
 
    @NotBlank(message = "description is required")
    private String description;
 
    @NotNull(message = "dueDate is required")
    @FutureOrPresent(message = "dueDate must be today or in the future")
    private LocalDate dueDate;
}