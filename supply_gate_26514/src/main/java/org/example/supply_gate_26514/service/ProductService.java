package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.ProductDto;
import org.example.supply_gate_26514.dto.ProductResponseDto;
import org.example.supply_gate_26514.mapper.ProductMapper;
import org.example.supply_gate_26514.model.Category;
import org.example.supply_gate_26514.model.Product;
import org.example.supply_gate_26514.model.ProductImage;
import org.example.supply_gate_26514.model.Store;
import org.example.supply_gate_26514.repository.CategoryRepository;
import org.example.supply_gate_26514.repository.ProductImageRepository;
import org.example.supply_gate_26514.repository.ProductRepository;
import org.example.supply_gate_26514.repository.StoreRepository;
import org.example.supply_gate_26514.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private org.example.supply_gate_26514.repository.ProductViewRepository productViewRepository;
    @Autowired
    private org.example.supply_gate_26514.repository.ProductLikeRepository productLikeRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private FileStorageService fileStorageService;

//    public List<ProductResponseDto> getAllProducts() {
//        return productRepository.findAll().stream().map(productMapper::transformToProductResponseDto).collect(Collectors.toList());
//    }
    /**
     * Gets all products (paginated) with optional search.
     * 
     * @param pageable Pagination parameters
     * @param search Optional search term to filter products (searches in productName, productDescription, categoryName, storeName, supplier info)
     * @return Paginated products
     */
    public Page<ProductResponseDto> getAllProducts(Pageable pageable, String search) {
        // Fetch products with relationships to avoid N+1 queries
        Page<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productRepository.findBySearch(search.trim(), pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        
        return products.map(product -> {
            // Ensure relationships are loaded
            if (product.getStore() != null && product.getStore().getUser() != null) {
                // Trigger lazy loading if needed - verification is accessed via repository, not direct relationship
                product.getStore().getUser().getUserId(); // Just ensure user is loaded
            }
            if (product.getProductImages() != null) {
                product.getProductImages().size(); // Trigger lazy load
            }
            return productMapper.transformToProductResponseDto(product);
        });
    }
    
    /**
     * Gets all products for public website display.
     * SECURITY: Only returns products from verified suppliers (APPROVED status).
     * This enforces filtering at the database level, not just in the UI.
     * 
     * @param pageable Pagination parameters
     * @param search Optional search term to filter products
     * @return Page of products from verified suppliers only
     */
    public Page<ProductResponseDto> getPublicProducts(Pageable pageable, String search) {
        Page<Product> products;
        
        if (search != null && !search.trim().isEmpty()) {
            // Search with verification filter
            products = productRepository.findByVerifiedSuppliersAndSearch(search.trim(), pageable);
        } else {
            // All verified products
            products = productRepository.findByVerifiedSuppliers(pageable);
        }
        
        return products.map(product -> {
            // Ensure relationships are loaded
            if (product.getStore() != null && product.getStore().getUser() != null) {
                product.getStore().getUser().getUserId(); // Trigger lazy load
            }
            if (product.getProductImages() != null) {
                product.getProductImages().size(); // Trigger lazy load
            }
            return productMapper.transformToProductResponseDto(product);
        });
    }
    public ProductResponseDto getProductById(UUID id) {
        return productRepository.findById(id).map(productMapper::transformToProductResponseDto).orElse(null);
    }
    public ProductResponseDto addAProduct(ProductDto productDto) {
        var product=productMapper.transformToProductDto(productDto);
        var savedProduct=productRepository.save(product);
        return productMapper.transformToProductResponseDto(savedProduct);
    }
    public ProductResponseDto updateProduct(UUID id, ProductDto productDto) {
        var existingProduct=productRepository.findById(id).orElse(new Product());
        existingProduct.setProductName(productDto.productName());
        existingProduct.setProductDescription(productDto.productDescription());
        existingProduct.setProductPrice(productDto.productPrice());
        var newStore =storeRepository.findById(productDto.storeId()).orElse(new Store());
        existingProduct.setStore(newStore);
        var newCategory =categoryRepository.findById(productDto.categoryId()).orElse(new Category());
        existingProduct.setCategory(newCategory);
        var updatedProduct=productRepository.save(existingProduct);

        return productMapper.transformToProductResponseDto(productRepository.save(updatedProduct));
    }

    public String deleteProduct(UUID id) {
        productRepository.deleteAll();
        return "Product deleted";
    }

    /**
     * Track a product view/impression.
     * Modern apps track this for analytics (Instagram, Facebook, Amazon).
     * 
     * @param productId Product ID to track view for
     */
    public void trackProductView(UUID productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
            
            org.example.supply_gate_26514.model.ProductView productView = new org.example.supply_gate_26514.model.ProductView();
            productView.setProduct(product);
            // Optional: Could extract IP from HttpServletRequest if needed
            productViewRepository.save(productView);
        } catch (Exception e) {
            // Log error but don't throw - tracking should not break user experience
            System.err.println("Failed to track product view: " + e.getMessage());
        }
    }

    /**
     * Track a product like/favorite.
     * Modern apps track this for engagement metrics (Instagram, Pinterest).
     * 
     * @param productId Product ID to track like for
     * @param isLiked Whether the product is being liked (true) or unliked (false)
     */
    public void trackProductLike(UUID productId, boolean isLiked) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
            
            org.example.supply_gate_26514.model.ProductLike productLike = new org.example.supply_gate_26514.model.ProductLike();
            productLike.setProduct(product);
            productLike.setActive(isLiked);
            // Optional: Could extract IP or user ID from HttpServletRequest if needed
            productLikeRepository.save(productLike);
        } catch (Exception e) {
            // Log error but don't throw - tracking should not break user experience
            System.err.println("Failed to track product like: " + e.getMessage());
        }
    }
    
    /**
     * Upload and save product images.
     * Stores files on disk and creates ProductImage entities linked to the product.
     * 
     * @param productId Product ID to attach images to
     * @param files List of image files to upload
     * @return List of saved image URLs (relative paths)
     * @throws IOException if file storage fails
     */
    @Transactional
    public List<String> uploadProductImages(UUID productId, List<MultipartFile> files) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        List<String> imageUrls = new ArrayList<>();
        
        // Store files and create ProductImage entities
        List<String> storedPaths = fileStorageService.storeImages(files, "products");
        
        for (String storedPath : storedPaths) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setImageUrl(storedPath);
            productImageRepository.save(productImage);
            imageUrls.add(storedPath);
        }
        
        return imageUrls;
    }
}
