package com.medichain.notification.repository;
 
import com.medichain.notification.entity.Task;
import com.medichain.notification.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.time.LocalDate;
import java.util.List;
 
public interface TaskRepository extends JpaRepository<Task, Long> {
 
    List<Task> findByAssignedToOrderByDueDateAsc(Long assignedTo);
 
    List<Task> findByAssignedToAndStatusOrderByDueDateAsc(Long assignedTo, TaskStatus status);
 
    List<Task> findByStatusOrderByDueDateAsc(TaskStatus status);
 
    // Mark all PENDING tasks past their due date as OVERDUE
    @Modifying
    @Query("UPDATE Task t SET t.status = 'OVERDUE' " +
           "WHERE t.status = 'PENDING' AND t.dueDate < :today")
    int markOverdueTasks(@Param("today") LocalDate today);
}