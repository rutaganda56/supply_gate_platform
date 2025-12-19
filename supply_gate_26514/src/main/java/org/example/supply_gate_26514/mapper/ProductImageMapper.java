package org.example.supply_gate_26514.mapper;

import org.example.supply_gate_26514.dto.ProductImageDto;
import org.example.supply_gate_26514.dto.ProductImageResponseDto;
import org.example.supply_gate_26514.model.Product;
import org.example.supply_gate_26514.model.ProductImage;
import org.springframework.stereotype.Service;

@Service
public class ProductImageMapper {

    public ProductImage transformToDto(ProductImageDto productImageDto) {
        ProductImage productImage = new ProductImage();
        productImage.setImageUrl(productImageDto.imageUrl());
        Product product = new Product();
        product.setProductId(productImageDto.productId());
        productImage.setProduct(product);
        return productImage;

    }
    public ProductImageResponseDto transformToResponseDto(ProductImage productImage) {
        return new ProductImageResponseDto(productImage.getImageUrl());
    }
}
