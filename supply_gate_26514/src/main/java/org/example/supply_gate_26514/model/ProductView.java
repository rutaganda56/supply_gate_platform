package org.example.supply_gate_26514.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ProductView entity for tracking product views/impressions.
 * Used for analytics and dashboard charts.
 * Similar to how Instagram, Facebook, and Amazon track product views.
 */
@Entity
@Table(name = "product_views")
public class ProductView {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID viewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference("product-view")
    private Product product;
    
    @CreationTimestamp
    private LocalDateTime viewedAt;
    
    // Optional: Track IP or user identifier for analytics
    private String viewerIdentifier; // IP address or session ID
    
    public ProductView() {
    }
    
    public UUID getViewId() {
        return viewId;
    }
    
    public void setViewId(UUID viewId) {
        this.viewId = viewId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public LocalDateTime getViewedAt() {
        return viewedAt;
    }
    
    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
    
    public String getViewerIdentifier() {
        return viewerIdentifier;
    }
    
    public void setViewerIdentifier(String viewerIdentifier) {
        this.viewerIdentifier = viewerIdentifier;
    }
}

