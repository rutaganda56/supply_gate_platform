package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    /**
     * Finds stores with search across multiple fields.
     * Searches in: storeName, storeEmail, phoneNumber
     */
    @Query("SELECT s FROM Store s WHERE " +
           "(LOWER(s.storeName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.storeEmail) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(s.phoneNumber, '')) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Store> findBySearch(@Param("search") String search, Pageable pageable);
}
