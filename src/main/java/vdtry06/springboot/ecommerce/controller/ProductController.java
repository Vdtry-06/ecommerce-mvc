package vdtry06.springboot.ecommerce.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.service.ProductService;
import vdtry06.springboot.ecommerce.dto.request.ProductRequest;
import vdtry06.springboot.ecommerce.dto.response.ProductResponse;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {
    private final ProductService productService;

    @PostMapping("/add")
    public ApiResponse<ProductResponse> addProduct(@ModelAttribute ProductRequest request) {
        ProductResponse response = productService.addProduct(request);
        return ApiResponse.<ProductResponse>builder()
                .data(response)
                .build();
    }

    @PutMapping("/update/{id}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @ModelAttribute ProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ApiResponse.<ProductResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/get/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        ProductResponse response = productService.getProduct(id);
        return ApiResponse.<ProductResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<ProductResponse>> getAllProduct() {
        List<ProductResponse> products = productService.getAllProducts();
        return ApiResponse.<List<ProductResponse>>builder()
                .data(products)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.<String>builder()
                .data(String.format("Product with id %s deleted", id))
                .build();
    }

    @GetMapping("/get-name-products/{name}")
    public ApiResponse<List<ProductResponse>> getProductByName(@PathVariable String name) {
        List<ProductResponse> products = productService.getNameProducts(name);
        return ApiResponse.<List<ProductResponse>>builder()
                .data(products)
                .build();
    }

    @GetMapping("/categories/filter")
    public ApiResponse<List<ProductResponse>> getProductsByCategories(
            @RequestParam List<Long> categoryIds) {
        return ApiResponse.<List<ProductResponse>>builder()
                .data(productService.getProductsByCategories(categoryIds))
                .build();
    }

}
