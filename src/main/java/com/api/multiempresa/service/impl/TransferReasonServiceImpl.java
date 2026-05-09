package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.filter.TransferReasonFilter;
import com.api.multiempresa.dto.mapper.TransferReasonMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.TransferReasonResponse;
import com.api.multiempresa.repository.TransferReasonRepository;
import com.api.multiempresa.repository.spec.TransferReasonSpecification;
import com.api.multiempresa.service.TransferReasonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferReasonServiceImpl implements TransferReasonService {

  private final TransferReasonRepository repository;
  private final TransferReasonMapper mapper;

  @Override
  public ApiResponse<List<TransferReasonResponse>> findAll(TransferReasonFilter filter) {
    return new ApiResponse<>("Motivos de traslado listados correctamente",
        mapper.toResponseList(repository.findAll(TransferReasonSpecification.byFilter(filter))));
  }
}
