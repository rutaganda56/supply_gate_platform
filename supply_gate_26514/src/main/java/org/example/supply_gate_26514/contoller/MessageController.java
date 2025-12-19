package org.example.supply_gate_26514.contoller;

import org.example.supply_gate_26514.dto.MessageDto;
import org.example.supply_gate_26514.dto.MessageResponseDto;
import org.example.supply_gate_26514.service.MessageService;
import org.example.supply_gate_26514.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private SecurityUtils securityUtils;

    /**
     * Public endpoint: Send a message to a supplier from website.
     * No authentication required - allows visitors to contact suppliers.
     * 
     * SECURITY: Validates message content, prevents spam, and ensures supplier exists.
     */
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> sendMessage(@Valid @RequestBody MessageDto messageDto) {
        try {
            MessageResponseDto response = messageService.sendMessage(messageDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // Return error message for better user feedback
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new java.util.HashMap<String, String>() {{
                        put("error", e.getMessage());
                        put("message", e.getMessage());
                    }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new java.util.HashMap<String, String>() {{
                        put("error", "Failed to send message. Please try again.");
                        put("message", "An error occurred while sending your message.");
                    }});
        }
    }

    /**
     * Get all messages for the authenticated supplier (paginated with search).
     * Requires authentication.
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param search Optional search term to filter messages
     */
    @GetMapping("/my-messages")
    public ResponseEntity<Page<MessageResponseDto>> getMyMessages(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String search) {
        try {
            UUID supplierId = securityUtils.getCurrentUserId();
            Page<MessageResponseDto> messages = messageService.getSupplierMessages(supplierId, pageable, search);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Get unread messages for the authenticated supplier.
     * Requires authentication.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<MessageResponseDto>> getUnreadMessages() {
        try {
            UUID supplierId = securityUtils.getCurrentUserId();
            List<MessageResponseDto> messages = messageService.getUnreadMessages(supplierId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Get unread message count for the authenticated supplier.
     * Requires authentication.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount() {
        try {
            UUID supplierId = securityUtils.getCurrentUserId();
            long count = messageService.getUnreadMessageCount(supplierId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Mark a message as read.
     * Requires authentication.
     */
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID messageId) {
        try {
            UUID supplierId = securityUtils.getCurrentUserId();
            messageService.markMessageAsRead(messageId, supplierId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var errors = new HashMap<String, String>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var errorMsg = error.getDefaultMessage();
            errors.put(fieldName, errorMsg);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}

