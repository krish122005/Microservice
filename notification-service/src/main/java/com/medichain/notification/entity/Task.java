package com.medichain.notification.entity;
 
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private Long assignedTo;           // UserID of the person responsible
 
    private Long relatedEntityId;      // e.g. RequestID, DeliveryID, InvoiceID
 
    @Column(nullable = false)
    private String relatedEntityType;  // e.g. "REQUEST", "DELIVERY", "INVOICE"
 
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
 
    @Column(nullable = false)
    private LocalDate dueDate;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
 
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
}