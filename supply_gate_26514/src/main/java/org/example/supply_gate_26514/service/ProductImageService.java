package org.example.supply_gate_26514.service;

import org.example.supply_gate_26514.dto.ProductImageDto;
import org.example.supply_gate_26514.dto.ProductImageResponseDto;
import org.example.supply_gate_26514.mapper.ProductImageMapper;
import org.example.supply_gate_26514.model.Product;
import org.example.supply_gate_26514.model.ProductImage;
import org.example.supply_gate_26514.repository.ProductImageRepository;
import org.example.supply_gate_26514.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private ProductImageMapper productImageMapper;
    @Autowired
    private ProductRepository productRepository;

    public List<ProductImageResponseDto> getAllProductImages() {
        return productImageRepository.findAll().stream().map(productImageMapper::transformToResponseDto).collect(Collectors.toList());
    }
    public ProductImageResponseDto addProductImage(ProductImageDto productImageDto) {
        var productImage = productImageMapper.transformToDto(productImageDto);
        var savedProductImage=productImageRepository.save(productImage);
        return productImageMapper.transformToResponseDto(savedProductImage);
    }
    public ProductImageResponseDto updateProductImage(UUID imageId, ProductImageDto productImageDto) {
        var existingProductImage = productImageRepository.findById(imageId).orElse(new ProductImage());
        existingProductImage.setImageUrl(productImageDto.imageUrl());
        var newProduct=productRepository.findById(productImageDto.productId()).orElse(new Product());
        existingProductImage.setProduct(newProduct);
        var savedProductImage=productImageRepository.save(existingProductImage);
        return productImageMapper.transformToResponseDto(savedProductImage);


    }
    public String deleteProductImage(UUID imageId) {
        productImageRepository.deleteById(imageId);
        return "Product Image Deleted";
    }
}
