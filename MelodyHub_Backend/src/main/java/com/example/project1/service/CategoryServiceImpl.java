package com.example.project1.service;

import com.example.project1.dto.CategoryRequest;
import com.example.project1.dto.CategoryResponse;
import com.example.project1.entity.Category;
import com.example.project1.exception.ApiErrors;
import com.example.project1.repository.CategoryRepository;
import com.example.project1.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = validate(request, null);
        Category category = new Category();
        category.setCategoryName(name);
        category.setDescription(ApiErrors.optionalText(request.getDescription(), "Description", 500));
        return convertToResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return convertToResponse(find(id));
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = find(id);
        String name = validate(request, id);
        category.setCategoryName(name);
        category.setDescription(ApiErrors.optionalText(request.getDescription(), "Description", 500));
        return convertToResponse(categoryRepository.save(category));
    }

    @Override
    public String deleteCategory(Long id) {
        find(id);
        if (productRepository.existsByCategory_CategoryId(id)) {
            throw ApiErrors.conflict("This category still has products. Move or delete those products first.");
        }
        categoryRepository.deleteById(id);
        return "Category deleted successfully";
    }

    private Category find(Long id) {
        return categoryRepository.findById(ApiErrors.required(id, "Category id is required."))
                .orElseThrow(() -> ApiErrors.notFound("Category not found."));
    }

    /** Returns the cleaned name; names are unique regardless of upper/lower case. */
    private String validate(CategoryRequest request, Long currentId) {
        if (request == null) {
            throw ApiErrors.badRequest("Category details are required.");
        }
        String name = ApiErrors.requiredText(request.getCategoryName(), "Category name", 100);
        boolean taken = categoryRepository.findAll().stream()
                .anyMatch(c -> c.getCategoryName() != null && c.getCategoryName().trim().equalsIgnoreCase(name)
                        && (currentId == null || !c.getCategoryId().equals(currentId)));
        if (taken) {
            throw ApiErrors.conflict("A category with this name already exists.");
        }
        return name;
    }

    private CategoryResponse convertToResponse(Category category) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getDescription()
        );
    }
}
