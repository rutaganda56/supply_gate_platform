package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.NotificationResponseDto;
import org.example.supply_gate_26514.model.Message;
import org.example.supply_gate_26514.model.Notification;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.repository.MessageRepository;
import org.example.supply_gate_26514.repository.NotificationRepository;
import org.example.supply_gate_26514.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * Gets all notifications for a user (paginated) with optional search.
     * 
     * @param userId The user's ID
     * @param pageable Pagination parameters
     * @param search Optional search term to filter notifications (searches in message, type)
     * @return Paginated notifications
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getUserNotifications(UUID userId, Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return notificationRepository.findByUser_UserIdAndSearchOrderByCreatedAtDesc(
                    userId, search.trim(), pageable)
                    .map(this::mapToResponseDto);
        } else {
            return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable)
                    .map(this::mapToResponseDto);
        }
    }

    /**
     * Gets all notifications for a user.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getAllUserNotifications(UUID userId) {
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets unread notification count for a user.
     */
    public long getUnreadNotificationCount(UUID userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalse(userId);
    }

    /**
     * Marks a notification as read.
     */
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Verify the notification belongs to the user
        User user = notification.getUser();
        if (user == null || user.getUserId() == null) {
            throw new RuntimeException("Notification has no associated user");
        }
        
        if (!user.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Notification does not belong to this user");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Marks all notifications as read for a user.
     */
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Creates a notification for a user.
     */
    @Transactional
    public NotificationResponseDto createNotification(UUID userId, String message, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type != null ? type : "general");
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        return mapToResponseDto(saved);
    }

    /**
     * Gets sender email for a message notification.
     * For message notifications, tries to find the corresponding message and return sender email.
     * 
     * @param notificationId Notification ID
     * @param userId Current user ID (for security)
     * @return Sender email if found, empty string otherwise
     */
    @Transactional(readOnly = true)
    public String getSenderEmailForNotification(UUID notificationId, UUID userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return "";
        }
        
        Notification notification = notificationOpt.get();
        
        // Verify the notification belongs to the user
        User user = notification.getUser();
        if (user == null || user.getUserId() == null) {
            return "";
        }
        
        if (!user.getUserId().equals(userId)) {
            return "";
        }
        
        // Only process message-type notifications
        if (!"message".equals(notification.getType())) {
            return "";
        }
        
        // Try to extract email from notification message first
        // Format: "New message from {senderName} ({senderEmail}): {subject}"
        String notificationMessage = notification.getMessage();
        if (notificationMessage != null) {
            // Try to extract email from format: "New message from Name (email@example.com): Subject"
            Pattern emailPattern = Pattern.compile("\\(([^)]+@[^)]+)\\)");
            Matcher matcher = emailPattern.matcher(notificationMessage);
            if (matcher.find() && matcher.groupCount() >= 1) {
                return matcher.group(1).trim();
            }
        }
        
        // Fallback: Try to find the message by matching notification details
        // Find messages for this supplier around the notification creation time
        Page<Message> recentMessagesPage = messageRepository.findBySupplier_UserIdOrderByCreatedAtDesc(userId, 
                PageRequest.of(0, 50));
        List<Message> recentMessages = recentMessagesPage.getContent();
        
        // Try to match by subject (extract subject from notification message)
        if (notificationMessage != null && notificationMessage.contains(": ")) {
            String subject = notificationMessage.substring(notificationMessage.lastIndexOf(": ") + 2).trim();
            java.time.LocalDateTime notificationCreatedAt = notification.getCreatedAt();
            
            Optional<Message> matchingMessage = recentMessages.stream()
                    .filter(m -> subject.equals(m.getSubject()))
                    .filter(m -> {
                        // Only compare timestamps if both are not null
                        if (notificationCreatedAt == null || m.getCreatedAt() == null) {
                            return false;
                        }
                        return Math.abs(Duration.between(
                                m.getCreatedAt(), notificationCreatedAt).toMinutes()) < 5; // Within 5 minutes
                    })
                    .findFirst();
            
            if (matchingMessage.isPresent()) {
                return matchingMessage.get().getSenderEmail();
            }
        }
        
        return "";
    }

    /**
     * Maps Notification entity to NotificationResponseDto.
     */
    private NotificationResponseDto mapToResponseDto(Notification notification) {
        User user = notification.getUser();
        if (user == null) {
            // Handle case where user is null (shouldn't happen, but defensive programming)
            return new NotificationResponseDto(
                    notification.getNotificationId(),
                    null, // userId will be null
                    "Unknown User",
                    notification.getMessage(),
                    notification.getType(),
                    notification.isRead(),
                    notification.getCreatedAt()
            );
        }
        
        String userName = (user.getFirstName() != null ? user.getFirstName() : "") + 
                         (user.getLastName() != null ? " " + user.getLastName() : "").trim();
        if (userName.isEmpty()) {
            userName = user.getUsername() != null ? user.getUsername() : "Unknown User";
        }

        return new NotificationResponseDto(
                notification.getNotificationId(),
                user.getUserId(),
                userName,
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
