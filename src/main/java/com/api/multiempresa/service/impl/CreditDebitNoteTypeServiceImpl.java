package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.CreditDebitNoteType;
import com.api.multiempresa.dto.filter.CreditDebitNoteTypeFilter;
import com.api.multiempresa.dto.mapper.CreditDebitNoteMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CreditDebitNoteTypeResponse;
import com.api.multiempresa.repository.CreditDebitNoteTypeRepository;
import com.api.multiempresa.repository.spec.CreditDebitNoteTypeSpecification;
import com.api.multiempresa.service.CreditDebitNoteTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditDebitNoteTypeServiceImpl implements CreditDebitNoteTypeService {

  private final CreditDebitNoteTypeRepository repository;
  private final CreditDebitNoteMapper mapper;

  @Override
  public ApiResponse<List<CreditDebitNoteTypeResponse>> findAll(CreditDebitNoteTypeFilter filter) {
    List<CreditDebitNoteType> types =
        repository.findAll(CreditDebitNoteTypeSpecification.byFilter(filter));
    List<CreditDebitNoteTypeResponse> response =
        types.stream().map(mapper::toTypeResponse).toList();
    return new ApiResponse<>("Tipos de nota listados correctamente", response);
  }
}
