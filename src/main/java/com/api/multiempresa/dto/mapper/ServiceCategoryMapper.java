package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.ServiceCategory;
import com.api.multiempresa.dto.response.ServiceCategoryResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceCategoryMapper {
  ServiceCategoryResponse toResponse(ServiceCategory entity);
  List<ServiceCategoryResponse> toResponseList(List<ServiceCategory> entities);
}
