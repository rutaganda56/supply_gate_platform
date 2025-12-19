package org.example.supply_gate_26514.contoller;

import org.example.supply_gate_26514.dto.ChartDataPoint;
import org.example.supply_gate_26514.dto.DashboardStatsDto;
import org.example.supply_gate_26514.model.UserEnum;
import org.example.supply_gate_26514.service.DashboardService;
import org.example.supply_gate_26514.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private SecurityUtils securityUtils;

    /**
     * Gets supplier dashboard stats for the authenticated user.
     * 
     * SECURITY: User ID is extracted from JWT token, not from client input.
     * Only suppliers can access their own dashboard.
     * 
     * @return Dashboard stats for the authenticated supplier
     */
    @GetMapping("/supplier")
    public ResponseEntity<DashboardStatsDto> getSupplierDashboardStats() {
        // Extract userId from token only - never trust client input
        UUID currentUserId = securityUtils.getCurrentUserId();
        
        // Verify user is a supplier (check in service or here)
        var user = securityUtils.getCurrentUser();
        if (user.getUserType() != UserEnum.SUPPLIER) {
            return ResponseEntity.status(403).build();
        }
        
        DashboardStatsDto stats = dashboardService.getSupplierDashboardStats(currentUserId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Gets industry dashboard stats for authenticated industry workers.
     * 
     * SECURITY: Only industry workers can access this endpoint.
     * User role is verified from JWT token, not from client input.
     * 
     * @return Dashboard stats for industry workers
     */
    @GetMapping("/industry")
    public ResponseEntity<?> getIndustryDashboardStats() {
        try {
            // Only industry workers can access industry dashboard
            securityUtils.requireRole(UserEnum.INDUSTRY_WORKER);
            DashboardStatsDto stats = dashboardService.getIndustryDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (IllegalStateException e) {
            // Handle authentication/authorization errors
            // This can occur if:
            // 1. User is not authenticated (no token or invalid token)
            // 2. User is not found in database
            // 3. User does not have INDUSTRY_WORKER role
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("required role")) {
                // User doesn't have the required role
                return ResponseEntity.status(403)
                        .body("Access denied. Industry worker role required.");
            } else if (errorMessage != null && errorMessage.contains("not found")) {
                // User not found in database (may have been deleted)
                return ResponseEntity.status(401)
                        .body("User not found. Please login again.");
            } else {
                // General authentication error
                return ResponseEntity.status(401)
                        .body("Authentication failed. Please login again.");
            }
        } catch (Exception e) {
            // Unexpected error
            return ResponseEntity.status(500)
                    .body("Failed to load dashboard stats: " + e.getMessage());
        }
    }

    /**
     * Get viewers chart data (product views over last 7 days).
     * Shows daily product views for the authenticated supplier's products.
     * 
     * SECURITY: User ID is extracted from JWT token, not from client input.
     * Only suppliers can access their own chart data.
     * 
     * @return Chart data points for last 7 days
     */
    @GetMapping("/supplier/viewers-chart")
    public ResponseEntity<List<ChartDataPoint>> getViewersChartData() {
        try {
            UUID currentUserId = securityUtils.getCurrentUserId();
            
            // Verify user is a supplier
            var user = securityUtils.getCurrentUser();
            if (user.getUserType() != UserEnum.SUPPLIER) {
                return ResponseEntity.status(403).build();
            }
            
            List<ChartDataPoint> data = dashboardService.getViewersChartData(currentUserId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get impressions chart data (impressions including likes over last 9 days).
     * Shows daily impressions (views + likes) for the authenticated supplier's products.
     * 
     * SECURITY: User ID is extracted from JWT token, not from client input.
     * Only suppliers can access their own chart data.
     * 
     * @return Chart data points for last 9 days
     */
    @GetMapping("/supplier/impressions-chart")
    public ResponseEntity<List<ChartDataPoint>> getImpressionsChartData() {
        try {
            UUID currentUserId = securityUtils.getCurrentUserId();
            
            // Verify user is a supplier
            var user = securityUtils.getCurrentUser();
            if (user.getUserType() != UserEnum.SUPPLIER) {
                return ResponseEntity.status(403).build();
            }
            
            List<ChartDataPoint> data = dashboardService.getImpressionsChartData(currentUserId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
