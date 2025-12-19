package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    /**
     * Finds all messages for a specific supplier.
     */
    Page<Message> findBySupplier_UserIdOrderByCreatedAtDesc(UUID supplierId, Pageable pageable);
    
    /**
     * Finds messages for a supplier with search across multiple fields.
     * Searches in: senderName, senderEmail, subject, messageContent, productName
     */
    @Query("SELECT m FROM Message m WHERE m.supplier.userId = :supplierId " +
           "AND (LOWER(m.senderName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.senderEmail) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.subject) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.messageContent) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(m.productName, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findBySupplier_UserIdAndSearchOrderByCreatedAtDesc(
            @Param("supplierId") UUID supplierId,
            @Param("search") String search,
            Pageable pageable);
    
    /**
     * Finds unread messages for a specific supplier.
     */
    List<Message> findBySupplier_UserIdAndIsReadFalseOrderByCreatedAtDesc(UUID supplierId);
    
    /**
     * Counts unread messages for a specific supplier.
     */
    long countBySupplier_UserIdAndIsReadFalse(UUID supplierId);
}

