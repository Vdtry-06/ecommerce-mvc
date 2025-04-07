package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.mapper.ProductMapper;
import vdtry06.springboot.ecommerce.dto.request.ProductRequest;
import vdtry06.springboot.ecommerce.dto.response.ProductResponse;
import vdtry06.springboot.ecommerce.entity.Category;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.CategoryRepository;
import vdtry06.springboot.ecommerce.repository.ProductRepository;
import vdtry06.springboot.ecommerce.entity.Topping;
import vdtry06.springboot.ecommerce.repository.ToppingRepository;

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
    ToppingRepository toppingRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse addProduct(ProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_NAME_EXISTS);
        }

        if (request.getAvailableQuantity() < 0) {
            throw new AppException(ErrorCode.NEGATIVE_QUANTITY);
        }
        BigDecimal price = request.getPrice();
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.NEGATIVE_PRICE);
        }

        Category category = categoryRepository.findByName(request.getCategoryName())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        Product product = productMapper.toProduct(request);
        product.setCategory(category);

        String imageUrl = null;
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(request.getFile(), "E-commerce/products/" + request.getName());
        }
        product.setImageUrl(imageUrl);

        Set<Topping> toppings = request.getToppingNames().stream()
                .map(toppingName -> toppingRepository.findByName(toppingName)
                        .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_EXISTED)))
                .collect(Collectors.toSet());
        product.setToppings(toppings);

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

        if (request.getAvailableQuantity() != null) {
            product.setAvailableQuantity((int) Math.max(request.getAvailableQuantity(), 0));
            log.info("Update product available quantity: {}", product.getAvailableQuantity());
        }

        if (request.getPrice() != null) {
            BigDecimal price = request.getPrice();
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ErrorCode.NEGATIVE_PRICE);
            }
            product.setPrice(request.getPrice());
            log.info("Update product price: {}", product.getPrice());
        }

        if (request.getCategoryName() != null && !request.getCategoryName().isEmpty()) {
            Category category = categoryRepository.findByName(request.getCategoryName())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
            product.setCategory(category);
            log.info("Update product category: {}", product.getCategory());
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

        if (request.getToppingNames() != null) {
            Set<Topping> toppings = request.getToppingNames().stream()
                    .map(toppingName -> toppingRepository.findByName(toppingName)
                            .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_EXISTED)))
                    .collect(Collectors.toSet());
            product.setToppings(toppings);
            log.info("Update product Topping: {}", product.getToppings());
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
        if (products.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return products.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(productResponses, pageable, productPage.getTotalElements());
    }
}