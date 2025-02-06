package vdtry06.springboot.ecommerce.product;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.product.dto.ProductRequest;
import vdtry06.springboot.ecommerce.product.dto.ProductResponse;
import vdtry06.springboot.ecommerce.category.Category;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;
import vdtry06.springboot.ecommerce.category.CategoryRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse addProduct(ProductRequest request) {
        if(productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_NAME_EXISTS);
        }
        if(request.getAvailableQuantity() < 0) {
            throw new AppException(ErrorCode.NEGATIVE_QUANTITY);
        }
        BigDecimal price = request.getPrice();
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.NEGATIVE_PRICE);
        }
        Set<Category> categories = request.getCategoryNames().stream()
                .map(categoryName -> categoryRepository.findByName(categoryName)
                        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)))
                .collect(Collectors.toSet());

        Product product = productMapper.toProduct(request);
        product.setCategories(categories);
        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if(request.getName() != null && !product.getName().equals(request.getName())
                && productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_NAME_EXISTS);
        }
        if(request.getName() != null && !request.getName().isEmpty()) {
            product.setName(request.getName());
        }
        if(request.getDescription() != null && !product.getDescription().isEmpty()) {
            product.setDescription(request.getDescription());
        }
        if(request.getAvailableQuantity() != null) {
            product.setAvailableQuantity((int) Math.max(request.getAvailableQuantity(), 0));
        }
        if(request.getPrice() != null) {
            BigDecimal price = request.getPrice();
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ErrorCode.NEGATIVE_PRICE);
            }
            product.setPrice(request.getPrice());
        }

        Set<Category> categories = request.getCategoryNames().stream()
                .map(categoryName -> categoryRepository.findByName(categoryName)
                        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)))
                .collect(Collectors.toSet());
        product.setCategories(categories);

        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse getProduct(Long id) {
        return productMapper.toProductResponse(
                productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {
        if(!productRepository.existsById(id)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
    }
}
