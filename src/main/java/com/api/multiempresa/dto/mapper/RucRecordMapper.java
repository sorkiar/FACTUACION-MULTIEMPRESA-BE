package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.RucRecord;
import com.api.multiempresa.dto.external.apiperu.ExternalRucData;
import com.api.multiempresa.dto.response.RucRecordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RucRecordMapper {

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "ubigeoDept", expression = "java(extractUbigeo(data.getUbigeo(), 0))")
  @Mapping(target = "ubigeoProv", expression = "java(extractUbigeo(data.getUbigeo(), 1))")
  @Mapping(target = "ubigeoDist", expression = "java(extractUbigeo(data.getUbigeo(), 2))")
  @Mapping(target = "isRetentionAgent", expression = "java(mapStringToBoolean(data.getIsRetentionAgent()))")
  @Mapping(target = "isPerceptionAgent", expression = "java(mapStringToBoolean(data.getIsPerceptionAgent()))")
  @Mapping(target = "isPerceptionFuelAgent", expression = "java(mapStringToBoolean(data.getIsPerceptionFuelAgent()))")
  @Mapping(target = "isGoodTaxpayer", expression = "java(mapStringToBoolean(data.getIsGoodTaxpayer()))")
  RucRecord toEntity(ExternalRucData data);

  RucRecordResponse toResponse(RucRecord entity);

  default String extractUbigeo(String[] ubigeo, int index) {
    if (ubigeo == null || ubigeo.length <= index) return null;
    return ubigeo[index];
  }

  default Boolean mapStringToBoolean(String value) {
    return "SI".equalsIgnoreCase(value);
  }
}
