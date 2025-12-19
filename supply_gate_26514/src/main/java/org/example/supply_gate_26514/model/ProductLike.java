package org.example.supply_gate_26514.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ProductLike entity for tracking product likes/favorites.
 * Used for analytics and dashboard charts.
 * Similar to how Instagram, Pinterest, and Facebook track likes.
 */
@Entity
@Table(name = "product_likes")
public class ProductLike {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID likeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference("product-like")
    private Product product;
    
    @CreationTimestamp
    private LocalDateTime likedAt;
    
    private boolean isActive; // true for like, false for unlike
    
    // Optional: Track who liked (if authenticated)
    private String likerIdentifier; // IP address, session ID, or user ID
    
    public ProductLike() {
    }
    
    public UUID getLikeId() {
        return likeId;
    }
    
    public void setLikeId(UUID likeId) {
        this.likeId = likeId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public LocalDateTime getLikedAt() {
        return likedAt;
    }
    
    public void setLikedAt(LocalDateTime likedAt) {
        this.likedAt = likedAt;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getLikerIdentifier() {
        return likerIdentifier;
    }
    
    public void setLikerIdentifier(String likerIdentifier) {
        this.likerIdentifier = likerIdentifier;
    }
}

