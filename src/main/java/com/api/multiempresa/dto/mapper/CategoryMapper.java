package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Category;
import com.api.multiempresa.dto.response.CategoryResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
  CategoryResponse toResponse(Category entity);
  List<CategoryResponse> toResponseList(List<Category> entities);
}
