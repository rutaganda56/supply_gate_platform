package org.example.supply_gate_26514.contoller;

import org.example.supply_gate_26514.dto.ProductDto;
import org.example.supply_gate_26514.dto.ProductResponseDto;
import org.example.supply_gate_26514.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;


    /**
     * Get all products (paginated with search).
     * For anonymous users: Returns all products by default, with optional filter for verified suppliers only.
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @param search Optional search term to filter products
     * @param verifiedOnly Optional filter to show only products from verified suppliers (default: false)
     * @return Page of products (all products by default, or only verified if verifiedOnly=true)
     */
    @GetMapping("/getProducts")
    public Page<ProductResponseDto> getAllProducts(
            @PageableDefault(size = 10, sort = "productName") Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean verifiedOnly) {
        // If verifiedOnly is true, filter to only verified suppliers
        // Otherwise, return all products in the system
        if (verifiedOnly) {
            return productService.getPublicProducts(pageable, search);
        } else {
            return productService.getAllProducts(pageable, search);
        }
    }
    
    /**
     * Get all products including unverified suppliers (for authenticated admin/internal use).
     * SECURITY: This endpoint should be protected and only accessible to authorized users.
     * Currently kept for backward compatibility but should be secured in production.
     * 
     * @param pageable Pagination parameters
     * @param search Optional search term
     * @return Page of all products (including unverified)
     */
    @GetMapping("/all")
    public Page<ProductResponseDto> getAllProductsIncludingUnverified(
            @PageableDefault(size = 10, sort = "productName") Pageable pageable,
            @RequestParam(required = false) String search) {
        // For internal/admin use - shows all products regardless of verification status
        return productService.getAllProducts(pageable, search);
    }
    @PostMapping("createAProduct")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto createProduct(@RequestBody ProductDto productDto) {
        return productService.addAProduct(productDto);
    }
    @PutMapping("{id}")
    public ProductResponseDto updateProduct(@PathVariable("id") UUID id, @RequestBody ProductDto productDto) {
        return productService.updateProduct(id,productDto);
    }
    @DeleteMapping("deleteProduct/{id}")
    public void deleteProduct(@PathVariable("id") UUID id) {
        productService.deleteProduct(id);
    }

    /**
     * Track a product view/impression.
     * Public endpoint - no authentication required.
     * Modern apps track this for analytics (Instagram, Facebook, Amazon).
     * 
     * SECURITY: Rate limiting should be implemented to prevent abuse.
     */
    @PostMapping("/{productId}/track-view")
    public ResponseEntity<Void> trackProductView(@PathVariable UUID productId) {
        try {
            productService.trackProductView(productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Return 200 even on error - tracking should not break user experience
            // Log error for monitoring
            System.err.println("Failed to track product view: " + e.getMessage());
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Track a product like/favorite.
     * Public endpoint - no authentication required.
     * Modern apps track this for engagement metrics (Instagram, Pinterest).
     * 
     * SECURITY: Rate limiting should be implemented to prevent abuse.
     */
    @PostMapping("/{productId}/track-like")
    public ResponseEntity<Void> trackProductLike(
            @PathVariable UUID productId,
            @RequestBody(required = false) java.util.Map<String, Boolean> request) {
        try {
            boolean isLiked = request != null && request.getOrDefault("isLiked", true);
            productService.trackProductLike(productId, isLiked);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Return 200 even on error - tracking should not break user experience
            // Log error for monitoring
            System.err.println("Failed to track product like: " + e.getMessage());
            return ResponseEntity.ok().build();
        }
    }
    
    /**
     * Upload product images.
     * Accepts multiple image files and saves them to the product.
     * 
     * @param productId Product ID to attach images to
     * @param files List of image files (multipart/form-data)
     * @return List of uploaded image URLs
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<?> uploadProductImages(
            @PathVariable UUID productId,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("No files provided");
            }
            
            List<String> imageUrls = productService.uploadProductImages(productId, files);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("message", "Images uploaded successfully");
                put("imageUrls", imageUrls);
            }});
        } catch (IOException e) {
            System.err.println("Failed to upload product images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload images: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error uploading product images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
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
