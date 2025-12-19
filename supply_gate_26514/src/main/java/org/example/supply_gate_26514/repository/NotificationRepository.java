package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    /**
     * Finds all notifications for a user, ordered by creation date (newest first).
     */
    Page<Notification> findByUser_UserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Finds notifications for a user with search across multiple fields.
     * Searches in: message, type
     */
    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId " +
           "AND (LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(n.type, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByUser_UserIdAndSearchOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("search") String search,
            Pageable pageable);
    
    /**
     * Finds all notifications for a user, ordered by creation date (newest first).
     */
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Finds unread notifications for a user, ordered by creation date (newest first).
     */
    List<Notification> findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Counts unread notifications for a user.
     */
    long countByUser_UserIdAndIsReadFalse(UUID userId);
}
