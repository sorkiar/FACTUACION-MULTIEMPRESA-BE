package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.filter.UnitMeasureFilter;
import com.api.multiempresa.dto.mapper.UnitMeasureMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UnitMeasureResponse;
import com.api.multiempresa.repository.UnitMeasureRepository;
import com.api.multiempresa.repository.spec.UnitMeasureSpecification;
import com.api.multiempresa.service.UnitMeasureService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnitMeasureServiceImpl implements UnitMeasureService {
  private final UnitMeasureRepository repository;
  private final UnitMeasureMapper mapper;

  @Override
  public ApiResponse<List<UnitMeasureResponse>> findAll(UnitMeasureFilter filter) {
    return new ApiResponse<>(
        "Unidades de medida listadas correctamente",
        mapper.toResponseList(
            repository.findAll(UnitMeasureSpecification.byFilter(filter))
        )
    );
  }
}
