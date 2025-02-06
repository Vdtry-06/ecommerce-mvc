package vdtry06.springboot.ecommerce.category;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.category.dto.CategoryRequest;
import vdtry06.springboot.ecommerce.category.dto.CategoryResponse;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse addCategory(CategoryRequest request) {
        if(categoryRepository.existsByName((request.getName()))) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
        Category category = categoryMapper.toCategory(request);
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    // Add a warning for the product.
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        if(request.getName() != null && !request.getName().isEmpty()
                && !category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS);
        }

        if(request.getName() != null && !request.getName().isEmpty()) {
            category.setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            category.setDescription(request.getDescription());
        }

        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }


    // pagination
    @PreAuthorize("hasRole('ADMIN')")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<CategoryResponse> getNameCategories(String name) {
        List<Category> categories = categoryRepository.findByNameContaining(name);
        if(categories.isEmpty()) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
        }
        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }


    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse getCategoryById(Long id) {
        return categoryMapper.toCategoryResponse(
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategoryById(Long id) {
        if(!categoryRepository.existsById(id)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
        }
        categoryRepository.deleteById(id);
    }

}
