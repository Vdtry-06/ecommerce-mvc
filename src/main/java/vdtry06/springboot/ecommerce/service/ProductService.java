package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.dto.request.product.ProductPurchaseRequest;
import vdtry06.springboot.ecommerce.dto.request.product.ProductRequest;
import vdtry06.springboot.ecommerce.dto.response.product.ProductResponse;
import vdtry06.springboot.ecommerce.dto.response.product.ProductPurchaseResponse;
import vdtry06.springboot.ecommerce.entity.Category;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.mapper.ProductMapper;
import vdtry06.springboot.ecommerce.repository.CategoryRepository;
import vdtry06.springboot.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
            product.setAvailableQuantity(Math.max(request.getAvailableQuantity(), 0));
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

    @Transactional(rollbackFor = AppException.class)
    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> request) {
        // 1. Danh sách yêu cầu mua sản phẩm
        var productIds = request
                .stream()
                .map(ProductPurchaseRequest::getProductId)
                .toList();

        var storedProducts = productRepository.findAllByIdInOrderById(productIds);

        // 2. Kiểm tra tính hợp lệ của sản phẩm
        if(productIds.size() != storedProducts.size()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // sắp xếp list theo yêu cầu để khớp với danh sách storedRequest
        var sortedRequest = request
                .stream()
                .sorted(Comparator.comparing(ProductPurchaseRequest::getProductId))
                .toList();
        var purchasedProducts = new ArrayList<ProductPurchaseResponse>();

        for(int i = 0; i < storedProducts.size(); i++) {
            var product = storedProducts.get(i);
            var productRequest = sortedRequest.get(i);

            // 3. cập nhật số lượng hàng tồn kho cho từng sản phẩm
            if(product.getAvailableQuantity() < productRequest.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
            var newAvailableQuantity = product.getAvailableQuantity() - productRequest.getQuantity();
            product.setAvailableQuantity(newAvailableQuantity);
            productRepository.save(product);

            // 4. Tạo danh sách phản hồi hoặc hoàn tác giao dịch nếu có lỗi xảy ra
            purchasedProducts.add(productMapper.toProductPurchaseResponse(product, productRequest.getQuantity()));
        }
        return purchasedProducts;
    }
}
