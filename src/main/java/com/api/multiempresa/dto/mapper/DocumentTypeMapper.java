package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.DocumentType;
import com.api.multiempresa.dto.response.DocumentTypeResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentTypeMapper {
  @Mapping(target = "personTypeId", source = "personType.id")
  @Mapping(target = "personTypeName", source = "personType.name")
  DocumentTypeResponse toResponse(DocumentType entity);

  List<DocumentTypeResponse> toResponseList(List<DocumentType> entities);
}
