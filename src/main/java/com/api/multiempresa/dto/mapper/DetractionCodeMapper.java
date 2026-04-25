package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.DetractionCode;
import com.api.multiempresa.dto.response.DetractionCodeResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DetractionCodeMapper {
  DetractionCodeResponse toResponse(DetractionCode entity);
  List<DetractionCodeResponse> toResponseList(List<DetractionCode> entities);
}
