package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.filter.PersonTypeFilter;
import com.api.multiempresa.dto.mapper.PersonTypeMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.PersonTypeResponse;
import com.api.multiempresa.repository.PersonTypeRepository;
import com.api.multiempresa.repository.spec.PersonTypeSpecification;
import com.api.multiempresa.service.PersonTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonTypeServiceImpl implements PersonTypeService {
  private final PersonTypeRepository repository;
  private final PersonTypeMapper mapper;

  @Override
  public ApiResponse<List<PersonTypeResponse>> findAll(PersonTypeFilter filter) {
    return new ApiResponse<>("Tipos de persona listado correctamente",
        mapper.toResponseList(repository.findAll(PersonTypeSpecification.byFilter(filter))));
  }
}
