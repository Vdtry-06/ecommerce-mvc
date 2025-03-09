package vdtry06.springboot.ecommerce.product;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.cloudinary.CloudinaryService;
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
    CloudinaryService cloudinaryService;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse addProduct(ProductRequest request) {
        if(productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_NAME_EXISTS);
        }

        String imageUrl = null;
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(request.getFile(), "E-commerce/products/" + request.getName());
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
        product.setImageUrl(imageUrl);
        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getName() != null && !request.getName().isEmpty()) {
            if (!product.getName().equals(request.getName()) && productRepository.existsByName(request.getName())) {
                throw new AppException(ErrorCode.PRODUCT_NAME_EXISTS);
            }
            product.setName(request.getName());
            log.info("Update product name: {}", product.getName());
        }

        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            product.setDescription(request.getDescription());
            log.info("Update product description: {}", product.getDescription());
        }

        if(request.getAvailableQuantity() != null) {
            product.setAvailableQuantity((int) Math.max(request.getAvailableQuantity(), 0));
            log.info("Update product available quantity: {}", product.getAvailableQuantity());
        }

        if(request.getPrice() != null) {
            BigDecimal price = request.getPrice();
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ErrorCode.NEGATIVE_PRICE);
            }
            product.setPrice(request.getPrice());
            log.info("Update product price: {}", product.getPrice());
        }

        if (request.getCategoryNames() != null) {
            Set<Category> categories = request.getCategoryNames().stream()
                    .map(categoryName -> categoryRepository.findByName(categoryName)
                            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)))
                    .collect(Collectors.toSet());
            product.setCategories(categories);
            log.info("Update product categories: {}", product.getCategories());
        }

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            if (product.getImageUrl() != null) {
                log.info("Updating product image for product {}", product.getImageUrl());
                cloudinaryService.deleteFile(product.getImageUrl());
            }
            String imageUrl = cloudinaryService.uploadFile(request.getFile(), "E-commerce/products/" + product.getName());
            product.setImageUrl(imageUrl);
            log.info("Updated product image: {}", imageUrl);
        }

        productRepository.save(product);

        return productMapper.toProductResponse(product);
    }

    public ProductResponse getProduct(Long id) {
        return productMapper.toProductResponse(
                productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND)));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getImageUrl() != null) {
            cloudinaryService.deleteFile(product.getImageUrl());
        }
        productRepository.deleteById(id);
    }

    public List<ProductResponse> getNameProducts(String name) {
        List<Product> products = productRepository.findByNameContaining(name);
        if(products.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return products.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }
}
