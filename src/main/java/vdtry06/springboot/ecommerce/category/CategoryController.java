package vdtry06.springboot.ecommerce.category;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.core.ApiResponse;
import vdtry06.springboot.ecommerce.category.dto.CategoryRequest;
import vdtry06.springboot.ecommerce.category.dto.CategoryResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @PostMapping("/add")
    public ApiResponse<CategoryResponse> addCategory(@RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.addCategory(request);
        return ApiResponse.<CategoryResponse>builder()
                .data(categoryResponse)
                .build();
    }

    @PutMapping("/update/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.updateCategory(id, request);
        return ApiResponse.<CategoryResponse>builder()
                .data(categoryResponse)
                .build();
    }

    @GetMapping("/get-category/{id}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Long id) {
        CategoryResponse categoryResponse = categoryService.getCategoryById(id);
        return ApiResponse.<CategoryResponse>builder()
                .data(categoryResponse)
                .build();
    }

    @GetMapping("/get-categories")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ApiResponse.<List<CategoryResponse>>builder()
                .data(categories)
                .build();
    }

    @GetMapping("/get-name-categories/{name}")
    public ApiResponse<List<CategoryResponse>> getCategoryByName(@PathVariable String name) {
        List<CategoryResponse> categories = categoryService.getNameCategories(name);
        return ApiResponse.<List<CategoryResponse>>builder()
                .data(categories)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ApiResponse.<String>builder()
                .data(String.format("Category with id %s deleted", id))
                .build();
    }
}
