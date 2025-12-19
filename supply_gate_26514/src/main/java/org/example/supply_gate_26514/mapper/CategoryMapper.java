package org.example.supply_gate_26514.mapper;

import org.example.supply_gate_26514.dto.CategoryDto;
import org.example.supply_gate_26514.dto.CategoryResponseDto;
import org.example.supply_gate_26514.model.Category;
import org.example.supply_gate_26514.model.Product;
import org.example.supply_gate_26514.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryMapper {

    public Category transformCategoryToCategoryDto(CategoryDto categoryDto) {
        Category category = new Category();
        category.setCategoryName(categoryDto.categoryName());
        return category;
    }
    public CategoryResponseDto transformCategoryDtoToCategoryResponseDto(Category category) {
        return new CategoryResponseDto(
                category.getCategoryId(),
                category.getCategoryName()
        );
    }
}
