package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vdtry06.springboot.ecommerce.dto.request.product.ProductRequest;
import vdtry06.springboot.ecommerce.dto.response.product.ProductResponse;
import vdtry06.springboot.ecommerce.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductRequest product);

    @Mapping(target = "categories", source = "categories")
    ProductResponse toProductResponse(Product product);
}
