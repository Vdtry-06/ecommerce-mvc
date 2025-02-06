package vdtry06.springboot.ecommerce.category;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.category.dto.CategoryRequest;
import vdtry06.springboot.ecommerce.category.dto.CategoryResponse;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequest category);

    CategoryResponse toCategoryResponse(Category category);
}
