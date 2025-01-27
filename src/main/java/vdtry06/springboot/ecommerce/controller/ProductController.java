package vdtry06.springboot.ecommerce.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.request.product.ProductRequest;
import vdtry06.springboot.ecommerce.dto.response.product.ProductResponse;
import vdtry06.springboot.ecommerce.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductController {
    private final ProductService productService;

    @PostMapping("/add")
    public ApiResponse<ProductResponse> addProduct(@RequestBody ProductRequest request) {
        ProductResponse response = productService.addProduct(request);
        return ApiResponse.<ProductResponse>builder()
                .data(response)
                .build();
    }

    @PutMapping("/update/{id}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
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

}
