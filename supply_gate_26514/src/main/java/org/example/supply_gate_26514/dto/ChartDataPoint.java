package org.example.supply_gate_26514.dto;

/**
 * DTO for chart data points.
 * Used for dashboard charts (viewers and impressions).
 * 
 * @param name Day name or date (e.g., "Mon", "1", "Jan 15")
 * @param value Count value for that day
 */
public record ChartDataPoint(
    String name,  // Day name or date
    Long value    // Count value
) {}

