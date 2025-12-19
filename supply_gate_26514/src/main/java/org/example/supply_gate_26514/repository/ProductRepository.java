package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    /**
     * Finds all products with store and user relationships loaded.
     * This ensures supplier information is available without N+1 queries.
     * Note: Verification is fetched separately via VerificationRepository when needed.
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.store s " +
           "LEFT JOIN FETCH s.user u " +
           "LEFT JOIN FETCH p.productImages")
    java.util.List<Product> findAllWithRelationships();
    
    /**
     * Finds products with search across multiple fields.
     * Searches in: productName, productDescription, categoryName, storeName, supplier name/email
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN p.store s " +
           "LEFT JOIN s.user u " +
           "LEFT JOIN p.category c " +
           "WHERE (LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(p.productDescription, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(c.categoryName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(s.storeName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(u.firstName, '') || ' ' || COALESCE(u.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findBySearch(@Param("search") String search, Pageable pageable);
    
    /**
     * Finds products from verified suppliers only (APPROVED status).
     * SECURITY: Uses INNER JOIN to ensure only products from suppliers with APPROVED verification are returned.
     * For public/procurement pages - only shows products from verified suppliers.
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "INNER JOIN p.store s " +
           "INNER JOIN s.user u " +
           "LEFT JOIN p.category c " +
           "INNER JOIN org.example.supply_gate_26514.model.Verification v ON v.user.userId = u.userId " +
           "WHERE v.status = org.example.supply_gate_26514.model.VerificationStatus.APPROVED")
    Page<Product> findByVerifiedSuppliers(Pageable pageable);
    
    /**
     * Finds products from verified suppliers with search.
     * SECURITY: Uses INNER JOIN to ensure only products from suppliers with APPROVED verification are returned.
     * Only returns products from suppliers with APPROVED verification status.
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "INNER JOIN p.store s " +
           "INNER JOIN s.user u " +
           "LEFT JOIN p.category c " +
           "INNER JOIN org.example.supply_gate_26514.model.Verification v ON v.user.userId = u.userId " +
           "WHERE v.status = org.example.supply_gate_26514.model.VerificationStatus.APPROVED " +
           "AND (LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(p.productDescription, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(c.categoryName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(s.storeName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(u.firstName, '') || ' ' || COALESCE(u.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findByVerifiedSuppliersAndSearch(@Param("search") String search, Pageable pageable);
    
    /**
     * Counts products owned by a specific supplier (via store).
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.user.userId = :userId")
    long countByStore_User_UserId(@Param("userId") UUID userId);
}
