package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Service;
import com.api.multiempresa.dto.response.ServiceResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceMapper {
  @Mapping(source = "serviceCategory.id", target = "serviceCategoryId")
  @Mapping(source = "serviceCategory.name", target = "serviceCategoryName")
  @Mapping(source = "chargeUnit.id", target = "chargeUnitId")
  @Mapping(source = "chargeUnit.name", target = "chargeUnitName")
  @Mapping(source = "createdAt", target = "registrationDate")
  @Mapping(source = "detractionCode.id", target = "detractionId")
  @Mapping(source = "detractionCode.code", target = "detractionCode")
  @Mapping(source = "detractionCode.description", target = "detractionDescription")
  @Mapping(source = "detractionCode.percentage", target = "detractionPercentage")
  @Mapping(source = "detractionCode.minAmount", target = "detractionMinAmount")
  ServiceResponse toResponse(Service entity);
  List<ServiceResponse> toResponseList(List<Service> entities);
}
