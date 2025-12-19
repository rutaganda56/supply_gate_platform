package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, UUID> {
    /**
     * Finds product views for a list of products after a specific date.
     * Used for chart data aggregation.
     */
    @Query("SELECT pv FROM ProductView pv WHERE pv.product.productId IN :productIds AND pv.viewedAt >= :after ORDER BY pv.viewedAt DESC")
    List<ProductView> findByProduct_ProductIdInAndViewedAtAfter(
        @Param("productIds") List<UUID> productIds, 
        @Param("after") LocalDateTime after
    );
    
    /**
     * Counts product views for all products belonging to a supplier.
     * Used for dashboard stats.
     */
    @Query("SELECT COUNT(pv) FROM ProductView pv WHERE pv.product.store.user.userId = :userId")
    long countByProduct_Store_User_UserId(@Param("userId") UUID userId);
}

