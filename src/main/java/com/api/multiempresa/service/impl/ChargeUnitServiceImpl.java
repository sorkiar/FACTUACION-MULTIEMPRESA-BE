package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.filter.ChargeUnitFilter;
import com.api.multiempresa.dto.mapper.ChargeUnitMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ChargeUnitResponse;
import com.api.multiempresa.repository.ChargeUnitRepository;
import com.api.multiempresa.repository.spec.ChargeUnitSpecification;
import com.api.multiempresa.service.ChargeUnitService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChargeUnitServiceImpl implements ChargeUnitService {
  private final ChargeUnitRepository repository;
  private final ChargeUnitMapper mapper;

  @Override
  public ApiResponse<List<ChargeUnitResponse>> findAll(ChargeUnitFilter filter) {
    return new ApiResponse<>(
        "Unidades de cobro listadas correctamente",
        mapper.toResponseList(
            repository.findAll(ChargeUnitSpecification.byFilter(filter))
        )
    );
  }
}
