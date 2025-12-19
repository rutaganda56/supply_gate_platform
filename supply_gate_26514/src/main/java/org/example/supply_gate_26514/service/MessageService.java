package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.MessageDto;
import org.example.supply_gate_26514.dto.MessageResponseDto;
import org.example.supply_gate_26514.model.Message;
import org.example.supply_gate_26514.model.Notification;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.repository.MessageRepository;
import org.example.supply_gate_26514.repository.NotificationRepository;
import org.example.supply_gate_26514.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Sends a message to a supplier and creates a notification.
     * This is the main method for website visitors to contact suppliers.
     * 
     * SECURITY: Validates supplier exists and enforces message content rules.
     */
    @Transactional
    public MessageResponseDto sendMessage(MessageDto messageDto) {
        // Validate supplier exists
        User supplier = userRepository.findById(messageDto.supplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + messageDto.supplierId()));

        // Additional validation: prevent empty or spam messages
        if (messageDto.messageContent() == null || messageDto.messageContent().trim().isEmpty()) {
            throw new RuntimeException("Message content cannot be empty");
        }
        
        if (messageDto.messageContent().trim().length() < 10) {
            throw new RuntimeException("Message must be at least 10 characters long");
        }

        // Create and save the message
        Message message = new Message();
        message.setSupplier(supplier);
        message.setSenderName(messageDto.senderName().trim());
        message.setSenderEmail(messageDto.senderEmail().trim().toLowerCase());
        message.setSenderPhone(messageDto.senderPhone() != null ? messageDto.senderPhone().trim() : null);
        message.setSubject(messageDto.subject().trim());
        message.setMessageContent(messageDto.messageContent().trim());
        message.setProductId(messageDto.productId());
        message.setProductName(messageDto.productName() != null ? messageDto.productName().trim() : null);
        message.setRead(false);

        Message savedMessage = messageRepository.save(message);

        // Create a notification for the supplier
        // Format: "New message from {senderName} ({senderEmail}): {subject}"
        // This allows the frontend to extract the email for reply functionality
        Notification notification = new Notification();
        notification.setUser(supplier);
        notification.setMessage(String.format("New message from %s (%s): %s", 
                messageDto.senderName().trim(),
                messageDto.senderEmail().trim(),
                messageDto.subject().trim()));
        notification.setType("message");
        notification.setRead(false);
        notificationRepository.save(notification);

        return mapToResponseDto(savedMessage);
    }

    /**
     * Gets all messages for a supplier (paginated) with optional search.
     * 
     * @param supplierId The supplier's user ID
     * @param pageable Pagination parameters
     * @param search Optional search term to filter messages (searches in senderName, senderEmail, subject, messageContent, productName)
     * @return Paginated messages
     */
    public Page<MessageResponseDto> getSupplierMessages(UUID supplierId, Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return messageRepository.findBySupplier_UserIdAndSearchOrderByCreatedAtDesc(
                    supplierId, search.trim(), pageable)
                    .map(this::mapToResponseDto);
        } else {
            return messageRepository.findBySupplier_UserIdOrderByCreatedAtDesc(supplierId, pageable)
                    .map(this::mapToResponseDto);
        }
    }

    /**
     * Gets unread messages for a supplier.
     */
    public List<MessageResponseDto> getUnreadMessages(UUID supplierId) {
        return messageRepository.findBySupplier_UserIdAndIsReadFalseOrderByCreatedAtDesc(supplierId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Marks a message as read.
     */
    @Transactional
    public void markMessageAsRead(UUID messageId, UUID supplierId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Verify the message belongs to the supplier
        if (!message.getSupplier().getUserId().equals(supplierId)) {
            throw new RuntimeException("Unauthorized: Message does not belong to this supplier");
        }

        message.setRead(true);
        message.setReadAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    /**
     * Gets unread message count for a supplier.
     */
    public long getUnreadMessageCount(UUID supplierId) {
        return messageRepository.countBySupplier_UserIdAndIsReadFalse(supplierId);
    }

    /**
     * Maps Message entity to MessageResponseDto.
     */
    private MessageResponseDto mapToResponseDto(Message message) {
        return new MessageResponseDto(
                message.getMessageId(),
                message.getSupplier().getUserId(),
                message.getSupplier().getFirstName() + " " + message.getSupplier().getLastName(),
                message.getSupplier().getEmail(),
                message.getSenderName(),
                message.getSenderEmail(),
                message.getSenderPhone(),
                message.getSubject(),
                message.getMessageContent(),
                message.getProductId(),
                message.getProductName(),
                message.getCreatedAt(),
                message.isRead(),
                message.getReadAt()
        );
    }
}

