package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Product;
import com.api.multiempresa.dto.response.ProductResponse;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
  @Mapping(target = "categoryId", source = "category.id")
  @Mapping(target = "categoryName", source = "category.name")
  @Mapping(target = "unitMeasureId", source = "unitMeasure.id")
  @Mapping(target = "unitMeasureCode", source = "unitMeasure.code")
  @Mapping(target = "detractionId", source = "detractionCode.id")
  @Mapping(target = "detractionCode", source = "detractionCode.code")
  @Mapping(target = "detractionDescription", source = "detractionCode.description")
  @Mapping(target = "detractionPercentage", source = "detractionCode.percentage")
  @Mapping(target = "detractionMinAmount", source = "detractionCode.minAmount")
  ProductResponse toResponse(Product entity);
  List<ProductResponse> toResponseList(List<Product> entities);
}
