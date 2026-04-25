package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Ubigeo;
import com.api.multiempresa.dto.request.UbigeoRequest;
import com.api.multiempresa.dto.response.UbigeoResponse;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UbigeoMapper {

  @Mapping(target = "status", ignore = true)
  Ubigeo toEntity(UbigeoRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "ubigeo", ignore = true)
  @Mapping(target = "status", ignore = true)
  void updateEntity(@MappingTarget Ubigeo entity, UbigeoRequest request);

  UbigeoResponse toResponse(Ubigeo entity);

  List<UbigeoResponse> toResponseList(List<Ubigeo> entities);
}
