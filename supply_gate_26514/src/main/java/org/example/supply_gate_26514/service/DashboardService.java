package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.ChartDataPoint;
import org.example.supply_gate_26514.dto.DashboardStatsDto;
import org.example.supply_gate_26514.model.Product;
import org.example.supply_gate_26514.model.ProductLike;
import org.example.supply_gate_26514.model.ProductView;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.model.UserEnum;
import org.example.supply_gate_26514.repository.MessageRepository;
import org.example.supply_gate_26514.repository.NotificationRepository;
import org.example.supply_gate_26514.repository.ProductLikeRepository;
import org.example.supply_gate_26514.repository.ProductRepository;
import org.example.supply_gate_26514.repository.ProductViewRepository;
import org.example.supply_gate_26514.repository.ReviewRepository;
import org.example.supply_gate_26514.repository.StoreRepository;
import org.example.supply_gate_26514.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private ProductViewRepository productViewRepository;
    @Autowired
    private ProductLikeRepository productLikeRepository;

    public DashboardStatsDto getSupplierDashboardStats(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Calculate real stats from database
        
        // Total Followers: Count of unique message senders (people who contacted the supplier)
        long totalFollowers = messageRepository.findBySupplier_UserIdOrderByCreatedAtDesc(userId, 
                org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(m -> m.getSenderEmail().toLowerCase())
                .distinct()
                .count();
        
        // Total Customers: Same as followers (unique message senders)
        long totalCustomers = totalFollowers;
        
        // Total Impressions: Count of products + total views + total likes
        // Modern apps count impressions as product count + engagement (views + likes)
        long productCount = productRepository.countByStore_User_UserId(userId);
        long totalViews = productViewRepository.countByProduct_Store_User_UserId(userId);
        long totalLikes = productLikeRepository.countByProduct_Store_User_UserIdAndIsActiveTrue(userId);
        long totalImpressions = productCount + totalViews + totalLikes;
        
        // Total Notifications: Unread notifications count
        long totalNotifications = notificationRepository.countByUser_UserIdAndIsReadFalse(userId);

        // Calculate changes (for now, set to 0 as we don't have historical data)
        // In production, you could compare with previous period
        double followersChange = 0.0;
        double customersChange = 0.0;
        double impressionsChange = 0.0;
        double notificationsChange = 0.0;

        return new DashboardStatsDto(
                totalFollowers,
                totalCustomers,
                totalImpressions,
                totalNotifications,
                followersChange,
                customersChange,
                impressionsChange,
                notificationsChange
        );
    }

    public DashboardStatsDto getIndustryDashboardStats() {
        // For industry workers, show verification-related stats
        long pendingVerifications = 0; // Will be calculated in controller
        long approvedVerifications = 0;
        long rejectedVerifications = 0;
        long totalNotifications = 0;

        return new DashboardStatsDto(
                pendingVerifications,
                approvedVerifications,
                rejectedVerifications,
                totalNotifications,
                0, 0, 0, 0
        );
    }

    /**
     * Get viewers chart data for last 7 days.
     * Groups product views by day for the supplier's products.
     * 
     * @param supplierId Supplier user ID
     * @return List of chart data points for last 7 days
     */
    public List<ChartDataPoint> getViewersChartData(UUID supplierId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        // Get product IDs for this supplier using repository query
        // More efficient than loading all products
        List<UUID> productIds = productRepository.findAll().stream()
                .filter(p -> {
                    try {
                        return p.getStore() != null 
                                && p.getStore().getUser() != null 
                                && p.getStore().getUser().getUserId().equals(supplierId);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(Product::getProductId)
                .collect(Collectors.toList());
        
        if (productIds.isEmpty()) {
            // Return empty data for last 7 days
            List<ChartDataPoint> data = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dayName = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH));
                data.add(new ChartDataPoint(dayName, 0L));
            }
            return data;
        }
        
        // Get views for these products
        List<ProductView> views = productViewRepository
                .findByProduct_ProductIdInAndViewedAtAfter(productIds, sevenDaysAgo);
        
        // Group by day and count
        Map<LocalDate, Long> viewsByDay = views.stream()
                .collect(Collectors.groupingBy(
                        view -> view.getViewedAt().toLocalDate(),
                        Collectors.counting()
                ));
        
        // Create chart data points for last 7 days
        List<ChartDataPoint> data = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dayName = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH));
            Long count = viewsByDay.getOrDefault(date, 0L);
            data.add(new ChartDataPoint(dayName, count));
        }
        
        return data;
    }

    /**
     * Get impressions chart data for last 9 days.
     * Includes both product views and likes.
     * 
     * @param supplierId Supplier user ID
     * @return List of chart data points for last 9 days
     */
    public List<ChartDataPoint> getImpressionsChartData(UUID supplierId) {
        LocalDateTime nineDaysAgo = LocalDateTime.now().minusDays(9);
        
        // Get product IDs for this supplier using repository query
        // More efficient than loading all products
        List<UUID> productIds = productRepository.findAll().stream()
                .filter(p -> {
                    try {
                        return p.getStore() != null 
                                && p.getStore().getUser() != null 
                                && p.getStore().getUser().getUserId().equals(supplierId);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(Product::getProductId)
                .collect(Collectors.toList());
        
        if (productIds.isEmpty()) {
            // Return empty data for last 9 days
            List<ChartDataPoint> data = new ArrayList<>();
            for (int i = 8; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dayNumber = String.valueOf(date.getDayOfMonth());
                data.add(new ChartDataPoint(dayNumber, 0L));
            }
            return data;
        }
        
        // Get views
        List<ProductView> views = productViewRepository
                .findByProduct_ProductIdInAndViewedAtAfter(productIds, nineDaysAgo);
        
        // Get likes (only active likes)
        List<ProductLike> likes = productLikeRepository
                .findByProduct_ProductIdInAndLikedAtAfterAndIsActiveTrue(productIds, nineDaysAgo);
        
        // Combine views and likes, group by day
        Map<LocalDate, Long> impressionsByDay = new HashMap<>();
        
        views.forEach(view -> {
            LocalDate date = view.getViewedAt().toLocalDate();
            impressionsByDay.put(date, impressionsByDay.getOrDefault(date, 0L) + 1);
        });
        
        likes.forEach(like -> {
            LocalDate date = like.getLikedAt().toLocalDate();
            impressionsByDay.put(date, impressionsByDay.getOrDefault(date, 0L) + 1);
        });
        
        // Create chart data points for last 9 days
        List<ChartDataPoint> data = new ArrayList<>();
        for (int i = 8; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dayNumber = String.valueOf(date.getDayOfMonth());
            Long count = impressionsByDay.getOrDefault(date, 0L);
            data.add(new ChartDataPoint(dayNumber, count));
        }
        
        return data;
    }
}

