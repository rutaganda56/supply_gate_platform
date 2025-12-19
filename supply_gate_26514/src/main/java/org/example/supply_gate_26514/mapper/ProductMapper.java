package org.example.supply_gate_26514.mapper;

import org.example.supply_gate_26514.dto.ProductDto;
import org.example.supply_gate_26514.dto.ProductResponseDto;
import org.example.supply_gate_26514.model.Category;
import org.example.supply_gate_26514.model.Product;
import org.example.supply_gate_26514.model.ProductImage;
import org.example.supply_gate_26514.model.Store;
import org.example.supply_gate_26514.model.VerificationStatus;
import org.example.supply_gate_26514.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductMapper {

    @Autowired
    private VerificationRepository verificationRepository;

    public Product transformToProductDto(ProductDto productDto) {
        Product product = new Product();
        product.setProductDescription(productDto.productDescription());
        product.setProductName(productDto.productName());
        product.setProductPrice(productDto.productPrice());
        product.setQuantity(productDto.quantity());
        Category category = new Category();
        category.setCategoryId(productDto.categoryId());
        product.setCategory(category);
        Store store = new Store();
        store.setStoreId(productDto.storeId());
        product.setStore(store);
        return product;
    }
    
    public ProductResponseDto transformToProductResponseDto(Product product) {
        // Extract image URLs from product images
        java.util.List<String> imageUrls = null;
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            imageUrls = product.getProductImages().stream()
                    .map(org.example.supply_gate_26514.model.ProductImage::getImageUrl)
                    .filter(url -> url != null && !url.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Extract supplier information from Store -> User relationship
        UUID supplierId = null;
        String supplierName = null;
        String supplierEmail = null;
        Boolean isSupplierVerified = false;
        
        if (product.getStore() != null && product.getStore().getUser() != null) {
            org.example.supply_gate_26514.model.User supplier = product.getStore().getUser();
            supplierId = supplier.getUserId();
            supplierName = (supplier.getFirstName() != null ? supplier.getFirstName() : "") + 
                          (supplier.getLastName() != null ? " " + supplier.getLastName() : "").trim();
            if (supplierName.isEmpty()) {
                supplierName = supplier.getUsername(); // Fallback to username
            }
            supplierEmail = supplier.getEmail();
            
            // Check if supplier is verified by querying Verification repository
            try {
                var verificationOpt = verificationRepository.findByUser_UserId(supplierId);
                if (verificationOpt.isPresent()) {
                    var verification = verificationOpt.get();
                    if (verification.getStatus() != null &&
                        verification.getStatus() == VerificationStatus.APPROVED) {
                        isSupplierVerified = true;
                    }
                }
            } catch (Exception e) {
                // If verification doesn't exist or error occurs, supplier is not verified
                isSupplierVerified = false;
            }
        }
        
        return new ProductResponseDto(
                product.getProductId(),
                product.getProductName(),
                product.getProductDescription(),
                product.getProductPrice(),
                product.getQuantity(),
                product.getCategory() != null ? product.getCategory().getCategoryId() : null,
                product.getCategory() != null ? product.getCategory().getCategoryName() : null,
                product.getStore() != null ? product.getStore().getStoreId() : null,
                product.getStore() != null ? product.getStore().getStoreName() : null,
                imageUrls != null && !imageUrls.isEmpty() ? imageUrls : java.util.Collections.emptyList(),
                supplierId,
                supplierName,
                supplierEmail,
                isSupplierVerified
        );
    }

}
