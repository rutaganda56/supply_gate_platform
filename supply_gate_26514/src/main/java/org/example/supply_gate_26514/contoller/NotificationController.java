package org.example.supply_gate_26514.contoller;

import org.example.supply_gate_26514.dto.NotificationResponseDto;
import org.example.supply_gate_26514.service.NotificationService;
import org.example.supply_gate_26514.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SecurityUtils securityUtils;

    /**
     * Get all notifications for the authenticated user (paginated with search).
     * Requires authentication.
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param search Optional search term to filter notifications
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getNotifications(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String search) {
        try {
            UUID userId = securityUtils.getCurrentUserId();
            Page<NotificationResponseDto> notifications = notificationService.getUserNotifications(userId, pageable, search);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Get all notifications for the authenticated user (non-paginated).
     * Requires authentication.
     */
    @GetMapping("/all")
    public ResponseEntity<List<NotificationResponseDto>> getAllNotifications() {
        try {
            UUID userId = securityUtils.getCurrentUserId();
            List<NotificationResponseDto> notifications = notificationService.getAllUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Get unread notifications for the authenticated user.
     * Requires authentication.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications() {
        try {
            UUID userId = securityUtils.getCurrentUserId();
            List<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Get unread notification count for the authenticated user.
     * Requires authentication.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        try {
            UUID userId = securityUtils.getCurrentUserId();
            long count = notificationService.getUnreadNotificationCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Mark a notification as read.
     * Requires authentication.
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        try {
            UUID userId = securityUtils.getCurrentUserId();
            notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).build();
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Mark all notifications as read for the authenticated user.
     * Requires authentication.
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        try {
            UUID userId = securityUtils.getCurrentUserId();
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Get sender email for a message notification.
     * Requires authentication.
     * 
     * @param notificationId Notification ID
     * @return Sender email if found
     */
    @GetMapping("/{notificationId}/sender-email")
    public ResponseEntity<String> getSenderEmail(@PathVariable UUID notificationId) {
        try {
            UUID userId = securityUtils.getCurrentUserId();
            String senderEmail = notificationService.getSenderEmailForNotification(notificationId, userId);
            if (senderEmail != null && !senderEmail.isEmpty()) {
                return ResponseEntity.ok(senderEmail);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}
