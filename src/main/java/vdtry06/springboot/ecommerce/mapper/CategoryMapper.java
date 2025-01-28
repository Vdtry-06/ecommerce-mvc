package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.dto.request.CategoryRequest;
import vdtry06.springboot.ecommerce.dto.response.CategoryResponse;
import vdtry06.springboot.ecommerce.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequest category);

    CategoryResponse toCategoryResponse(Category category);
}
