package org.example.supply_gate_26514.mapper;

import org.example.supply_gate_26514.dto.ReviewDto;
import org.example.supply_gate_26514.dto.ReviewResponseDto;
import org.example.supply_gate_26514.model.Product;
import org.example.supply_gate_26514.model.Review;
import org.example.supply_gate_26514.model.User;
import org.springframework.stereotype.Service;

@Service
public class ReviewMapper {

    public Review transformToReviewDto(ReviewDto reviewDto) {
        Review review = new Review();
        review.setMessage(reviewDto.message());
        User user = new User();
        user.setUserId(reviewDto.userId());
        review.setUser(user);
        Product product = new Product();
        product.setProductId(reviewDto.productId());
        review.setProduct(product);
        return review;
    }

    public ReviewResponseDto transformToReviewResponseDto(Review review) {
        return new ReviewResponseDto(review.getMessage());
    }
}
