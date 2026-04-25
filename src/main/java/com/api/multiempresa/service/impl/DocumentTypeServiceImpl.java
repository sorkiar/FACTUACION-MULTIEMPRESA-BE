package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.filter.DocumentTypeFilter;
import com.api.multiempresa.dto.mapper.DocumentTypeMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DocumentTypeResponse;
import com.api.multiempresa.repository.DocumentTypeRepository;
import com.api.multiempresa.repository.spec.DocumentTypeSpecification;
import com.api.multiempresa.service.DocumentTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentTypeServiceImpl implements DocumentTypeService {
  private final DocumentTypeRepository repository;
  private final DocumentTypeMapper mapper;

  @Override
  public ApiResponse<List<DocumentTypeResponse>> findAll(DocumentTypeFilter filter) {
    return new ApiResponse<>("Tipos de documento listado correctamente",
        mapper.toResponseList(repository.findAll(DocumentTypeSpecification.byFilter(filter))));
  }
}
