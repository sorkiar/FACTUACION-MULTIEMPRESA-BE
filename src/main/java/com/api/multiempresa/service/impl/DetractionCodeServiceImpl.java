package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.filter.DetractionCodeFilter;
import com.api.multiempresa.dto.mapper.DetractionCodeMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DetractionCodeResponse;
import com.api.multiempresa.repository.DetractionCodeRepository;
import com.api.multiempresa.repository.spec.DetractionCodeSpecification;
import com.api.multiempresa.service.DetractionCodeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DetractionCodeServiceImpl implements DetractionCodeService {

  private final DetractionCodeRepository repository;
  private final DetractionCodeMapper mapper;

  @Override
  public ApiResponse<List<DetractionCodeResponse>> findAll(DetractionCodeFilter filter) {
    return new ApiResponse<>(
        "Códigos de detracción listados correctamente",
        mapper.toResponseList(
            repository.findAll(DetractionCodeSpecification.byFilter(filter))
        )
    );
  }
}
