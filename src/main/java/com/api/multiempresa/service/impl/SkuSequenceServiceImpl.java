package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.repository.SkuSequenceRepository;
import com.api.multiempresa.service.SkuSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkuSequenceServiceImpl implements SkuSequenceService {
  private final SkuSequenceRepository repository;

  @Override
  public ApiResponse<String> generateSkuPreview(String type) {
    int current = repository.getCurrentValue(type);
    String sku = String.format("%s%07d", type, current + 1);

    return new ApiResponse<>("Correlativo SKU listado correctamente", sku);
  }

  @Override
  public String registerSku(String type) {
    int next = repository.registerSku(type);

    return String.format("%s%07d", type, next);
  }
}
