package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.DniRecord;
import com.api.multiempresa.dto.external.apiperu.ExternalDniData;
import com.api.multiempresa.dto.response.DniRecordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DniRecordMapper {

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "ubigeoDept", expression = "java(extractUbigeo(data.getUbigeo(), 0))")
  @Mapping(target = "ubigeoProv", expression = "java(extractUbigeo(data.getUbigeo(), 1))")
  @Mapping(target = "ubigeoDist", expression = "java(extractUbigeo(data.getUbigeo(), 2))")
  DniRecord toEntity(ExternalDniData data);

  DniRecordResponse toResponse(DniRecord entity);

  default String extractUbigeo(String[] ubigeo, int index) {
    if (ubigeo == null || ubigeo.length <= index) return null;
    return ubigeo[index];
  }
}
