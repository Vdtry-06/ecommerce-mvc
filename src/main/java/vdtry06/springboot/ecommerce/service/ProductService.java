package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
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

        Set<Category> categories = request.getCategoryNames().stream()
                .map(categoryName -> categoryRepository.findByName(categoryName)
                        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)))
                .collect(Collectors.toSet());

        Product product = productMapper.toProduct(request);
        product.setCategories(categories);

        List<String> imageUrls = new ArrayList<>();
        if (request.getFiles() != null && request.getFiles().length > 0) {
            for (int i = 0; i < request.getFiles().length; i++) {
                if (!request.getFiles()[i].isEmpty()) {
                    String imageUrl = cloudinaryService.uploadFile(
                            request.getFiles()[i],
                            "E-commerce/products/" + request.getName() + "/image_" + i
                    );
                    imageUrls.add(imageUrl);
                }
            }
        }
        product.setImageUrls(imageUrls);

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

        if (request.getCategoryNames() != null) {
            Set<Category> categories = request.getCategoryNames().stream()
                    .map(categoryName -> categoryRepository.findByName(categoryName)
                            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)))
                    .collect(Collectors.toSet());
            product.setCategories(categories);
            log.info("Update product categories: {}", product.getCategories());
        }

        if (request.getFiles() != null && request.getFiles().length > 0) {
            // Delete existing images
            if (!product.getImageUrls().isEmpty()) {
                for (String imageUrl : product.getImageUrls()) {
                    cloudinaryService.deleteFile(imageUrl);
                }
                product.getImageUrls().clear();
            }
            // Upload new images
            List<String> newImageUrls = new ArrayList<>();
            for (int i = 0; i < request.getFiles().length; i++) {
                if (!request.getFiles()[i].isEmpty()) {
                    String imageUrl = cloudinaryService.uploadFile(
                            request.getFiles()[i],
                            "E-commerce/products/" + product.getName() + "/image_" + i
                    );
                    newImageUrls.add(imageUrl);
                }
            }
            product.setImageUrls(newImageUrls);
            log.info("Updated product images: {}", newImageUrls);
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

        if (!product.getImageUrls().isEmpty()) {
            for (String imageUrl : product.getImageUrls()) {
                cloudinaryService.deleteFile(imageUrl);
            }
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

    public List<ProductResponse> getProductsByCategories(List<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
        }
        List<Product> products = productRepository.findProductsByExactCategories(categoryIds, categoryIds.size());
        return products.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

}
