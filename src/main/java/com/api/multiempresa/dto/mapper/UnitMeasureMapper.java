package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.UnitMeasure;
import com.api.multiempresa.dto.response.UnitMeasureResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnitMeasureMapper {
  UnitMeasureResponse toResponse(UnitMeasure entity);
  List<UnitMeasureResponse> toResponseList(List<UnitMeasure> entities);
}
