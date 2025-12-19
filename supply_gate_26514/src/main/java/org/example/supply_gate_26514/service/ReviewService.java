package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.ReviewDto;
import org.example.supply_gate_26514.dto.ReviewResponseDto;
import org.example.supply_gate_26514.mapper.ReviewMapper;
import org.example.supply_gate_26514.model.Product;
import org.example.supply_gate_26514.model.Review;
import org.example.supply_gate_26514.model.User;
import org.example.supply_gate_26514.repository.ProductRepository;
import org.example.supply_gate_26514.repository.ReviewRepository;
import org.example.supply_gate_26514.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    public List<ReviewResponseDto> retrieveAllReviews() {
        return reviewRepository.findAll().stream().map(reviewMapper::transformToReviewResponseDto).collect(Collectors.toList());
    }
    
    /**
     * Adds a new review.
     * 
     * SECURITY: userId in DTO comes from authenticated token, not client input.
     * 
     * @param reviewDto Review data with userId from token
     * @return Created ReviewResponseDto
     */
    public ReviewResponseDto addAReview(ReviewDto reviewDto) {
        // userId in reviewDto is from authenticated token, validated by controller
        User user = userRepository.findById(reviewDto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Product product = productRepository.findById(reviewDto.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        var review = reviewMapper.transformToReviewDto(reviewDto);
        review.setUser(user);
        review.setProduct(product);
        
        var savedReview = reviewRepository.save(review);
        return reviewMapper.transformToReviewResponseDto(savedReview);
    }
    
    /**
     * Updates an existing review.
     * 
     * SECURITY: userId in DTO comes from authenticated token.
     * Only the review owner can update their review.
     * 
     * @param id Review ID
     * @param reviewDto Review data with userId from token
     * @return Updated ReviewResponseDto
     */
    public ReviewResponseDto updateAReview(UUID id, ReviewDto reviewDto) {
        var existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Verify that the authenticated user owns this review
        if (!existingReview.getUser().getUserId().equals(reviewDto.userId())) {
            throw new IllegalArgumentException("You can only update your own reviews");
        }
        
        // userId in reviewDto is from authenticated token, validated by controller
        User user = userRepository.findById(reviewDto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Product product = productRepository.findById(reviewDto.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        existingReview.setMessage(reviewDto.message());
        existingReview.setUser(user);
        existingReview.setProduct(product);
        
        var updatedReview = reviewRepository.save(existingReview);
        return reviewMapper.transformToReviewResponseDto(updatedReview);
    }
    
    /**
     * Deletes a review.
     * 
     * SECURITY: Only the review owner can delete their review.
     * userId is from authenticated token, not client input.
     * 
     * @param id Review ID
     * @param authenticatedUserId User ID from authenticated token
     * @return Success message
     */
    public String deleteAReview(UUID id, UUID authenticatedUserId) {
        var review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Verify that the authenticated user owns this review
        if (!review.getUser().getUserId().equals(authenticatedUserId)) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }
        
        reviewRepository.deleteById(id);
        return "Review deleted successfully";
    }
}
