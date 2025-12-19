package org.example.supply_gate_26514.contoller;

import org.example.supply_gate_26514.service.LocationDataInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Controller for Location Data Management
 * 
 * Provides endpoints to:
 * - Check if locations are initialized
 * - Manually trigger location data loading
 * - Get initialization status
 */
@RestController
@RequestMapping("/api/location/admin")
public class LocationAdminController {

    @Autowired
    private LocationDataInitializer locationDataInitializer;

    /**
     * Check if location data is initialized
     * GET /api/location/admin/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getInitializationStatus() {
        boolean initialized = locationDataInitializer.isInitialized();
        return ResponseEntity.ok(Map.of(
            "initialized", initialized,
            "message", initialized 
                ? "Location data is already initialized" 
                : "Location data is not initialized. Use POST /api/location/admin/initialize to load data."
        ));
    }

    /**
     * Manually trigger location data initialization
     * POST /api/location/admin/initialize
     * 
     * This is useful when:
     * - You want to control when data is loaded
     * - You disabled auto-initialization
     * - You need to reload data
     */
    @PostMapping("/initialize")
    public ResponseEntity<?> initializeLocations() {
        try {
            if (locationDataInitializer.isInitialized()) {
                return ResponseEntity.ok(Map.of(
                    "message", "Location data is already initialized",
                    "initialized", true
                ));
            }

            // Initialize in background (non-blocking)
            locationDataInitializer.initializeInBackground();
            
            return ResponseEntity.accepted().body(Map.of(
                "message", "Location data initialization started in background",
                "status", "processing"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to initialize locations",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Synchronously initialize locations (blocks until complete)
     * POST /api/location/admin/initialize-sync
     * 
     * Use this if you want to wait for completion
     */
    @PostMapping("/initialize-sync")
    public ResponseEntity<?> initializeLocationsSync() {
        try {
            if (locationDataInitializer.isInitialized()) {
                return ResponseEntity.ok(Map.of(
                    "message", "Location data is already initialized",
                    "initialized", true
                ));
            }

            long startTime = System.currentTimeMillis();
            locationDataInitializer.initializeKigaliLocations();
            long duration = System.currentTimeMillis() - startTime;

            return ResponseEntity.ok(Map.of(
                "message", "Location data initialized successfully",
                "duration_ms", duration,
                "initialized", true
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to initialize locations",
                "message", e.getMessage()
            ));
        }
    }
}

