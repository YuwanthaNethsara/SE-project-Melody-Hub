package com.example.project1.service;

import com.example.project1.dto.CategoryRequest;
import com.example.project1.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    String deleteCategory(Long id);
}
