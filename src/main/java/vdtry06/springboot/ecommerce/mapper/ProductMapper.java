package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.dto.request.ProductRequest;
import vdtry06.springboot.ecommerce.dto.response.ProductResponse;
import vdtry06.springboot.ecommerce.dto.response.ProductPurchaseResponse;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "toppings", ignore = true)
    Product toProduct(ProductRequest product);

    @Mapping(target = "categories", source = "categories")
    @Mapping(target = "toppings", source = "toppings")
    ProductResponse toProductResponse(Product product);

    ProductPurchaseResponse toProductPurchaseResponse(Product product, Double quantity);
}
