package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    /**
     * Finds categories with search by category name.
     */
    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Category> findBySearch(@Param("search") String search, Pageable pageable);
}
