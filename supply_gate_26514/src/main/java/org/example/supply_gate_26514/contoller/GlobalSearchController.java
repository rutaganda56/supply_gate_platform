package org.example.supply_gate_26514.contoller;

import org.example.supply_gate_26514.dto.GlobalSearchResultDto;
import org.example.supply_gate_26514.service.GlobalSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Global Search Controller
 * 
 * Provides unified search across multiple entities (products, stores, categories, verifications, messages).
 * Similar to command palette or global search in enterprise dashboards.
 */
@RestController
@RequestMapping("/api/search")
public class GlobalSearchController {

    @Autowired
    private GlobalSearchService globalSearchService;

    /**
     * Global search endpoint.
     * Searches across products, stores, categories, verifications, and messages.
     * SECURITY: Results are filtered based on user role and permissions.
     * 
     * @param q Search query string
     * @param limit Maximum results per category (default: 5)
     * @return GlobalSearchResultDto with categorized results
     */
    @GetMapping
    public ResponseEntity<GlobalSearchResultDto> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            // Limit results per category to prevent excessive data transfer
            int safeLimit = Math.min(Math.max(limit, 1), 10); // Between 1 and 10
            
            GlobalSearchResultDto results = globalSearchService.search(query, safeLimit);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            // Return empty results on error rather than failing
            return ResponseEntity.ok(new GlobalSearchResultDto(
                    new java.util.ArrayList<>(),
                    new java.util.ArrayList<>(),
                    new java.util.ArrayList<>(),
                    new java.util.ArrayList<>(),
                    new java.util.ArrayList<>(),
                    0
            ));
        }
    }
}
