package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, UUID> {
    /**
     * Finds active product likes for a list of products after a specific date.
     * Used for chart data aggregation.
     */
    @Query("SELECT pl FROM ProductLike pl WHERE pl.product.productId IN :productIds AND pl.likedAt >= :after AND pl.isActive = true ORDER BY pl.likedAt DESC")
    List<ProductLike> findByProduct_ProductIdInAndLikedAtAfterAndIsActiveTrue(
        @Param("productIds") List<UUID> productIds, 
        @Param("after") LocalDateTime after
    );
    
    /**
     * Counts active product likes for all products belonging to a supplier.
     * Used for dashboard stats.
     */
    @Query("SELECT COUNT(pl) FROM ProductLike pl WHERE pl.product.store.user.userId = :userId AND pl.isActive = true")
    long countByProduct_Store_User_UserIdAndIsActiveTrue(@Param("userId") UUID userId);
}

