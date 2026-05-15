package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.DocumentSeries;
import com.api.multiempresa.dto.response.DocumentSeriesResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentSeriesMapper {

  @Mapping(source = "documentTypeSunat.code", target = "documentTypeSunatCode")
  @Mapping(source = "documentTypeSunat.name", target = "documentTypeSunatName")
  @Mapping(source = "currentSequence", target = "sequence")
  @Mapping(source = "parentDocumentTypeCode", target = "parentDocumentTypeCode")
  DocumentSeriesResponse toResponse(DocumentSeries entity);
}
