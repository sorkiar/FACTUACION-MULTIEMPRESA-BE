package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.ChargeUnit;
import com.api.multiempresa.dto.response.ChargeUnitResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeUnitMapper {
  ChargeUnitResponse toResponse(ChargeUnit entity);
  List<ChargeUnitResponse> toResponseList(List<ChargeUnit> entities);
}
