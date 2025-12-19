package org.example.supply_gate_26514.contoller;

import jakarta.validation.Valid;
import org.example.supply_gate_26514.dto.ReviewDto;
import org.example.supply_gate_26514.dto.ReviewResponseDto;
import org.example.supply_gate_26514.service.ReviewService;
import org.example.supply_gate_26514.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/reviews")
    public List<ReviewResponseDto> getAllReviews() {
        return reviewService.retrieveAllReviews();

    }
    
    /**
     * Creates a new review.
     * 
     * SECURITY: User ID is extracted from JWT token, not from client input.
     * The userId in ReviewDto is ignored and replaced with authenticated user's ID.
     * 
     * @param reviewDto Review data (userId in DTO is ignored)
     * @return Created ReviewResponseDto
     */
    @PostMapping("/createReview")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ReviewResponseDto> AddReview(@RequestBody ReviewDto reviewDto) {
        try {
            // Extract userId from token - never trust client input
            UUID authenticatedUserId = securityUtils.getCurrentUserId();
            
            // Create new DTO with userId from token (ignore client-provided userId)
            ReviewDto secureDto = new ReviewDto(
                authenticatedUserId,
                reviewDto.productId(),
                reviewDto.message()
            );
            
            ReviewResponseDto response = reviewService.addAReview(secureDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Updates a review.
     * 
     * SECURITY: Users can only update their own reviews.
     * User ID is extracted from JWT token, not from client input.
     * 
     * @param id Review ID
     * @param reviewDto Review data (userId in DTO is ignored)
     * @return Updated ReviewResponseDto
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> updateReview(@PathVariable("id") UUID id, @Valid @RequestBody ReviewDto reviewDto) {
        try {
            // Extract userId from token - never trust client input
            UUID authenticatedUserId = securityUtils.getCurrentUserId();
            
            // Create new DTO with userId from token (ignore client-provided userId)
            ReviewDto secureDto = new ReviewDto(
                authenticatedUserId,
                reviewDto.productId(),
                reviewDto.message()
            );
            
            ReviewResponseDto response = reviewService.updateAReview(id, secureDto);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Deletes a review.
     * 
     * SECURITY: Users can only delete their own reviews.
     * 
     * @param id Review ID
     * @return Success response
     */
    @DeleteMapping("/deleteReview/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable("id") UUID id) {
        try {
            // Extract userId from token
            UUID authenticatedUserId = securityUtils.getCurrentUserId();
            
            // Service will verify ownership
            String result = reviewService.deleteAReview(id, authenticatedUserId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete review");
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
