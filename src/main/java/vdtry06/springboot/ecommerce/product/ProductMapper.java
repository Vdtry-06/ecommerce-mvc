package vdtry06.springboot.ecommerce.product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vdtry06.springboot.ecommerce.product.dto.ProductRequest;
import vdtry06.springboot.ecommerce.product.dto.ProductResponse;
import vdtry06.springboot.ecommerce.product.dto.ProductPurchaseResponse;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductRequest product);

    @Mapping(target = "toppings", source = "toppings")
    @Mapping(target = "categories", source = "categories")
    ProductResponse toProductResponse(Product product);

    ProductPurchaseResponse toProductPurchaseResponse(Product product, Double quantity);
}
