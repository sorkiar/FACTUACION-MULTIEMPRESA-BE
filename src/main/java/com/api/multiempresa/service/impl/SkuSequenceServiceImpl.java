package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.repository.SkuSequenceRepository;
import com.api.multiempresa.service.SkuSequenceService;
import com.api.multiempresa.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkuSequenceServiceImpl implements SkuSequenceService {

  private final SkuSequenceRepository repository;

  @Override
  public ApiResponse<String> generateSkuPreview(String type) {
    Long companyId = TenantContext.getCompanyId();
    int current = repository.getCurrentValue(type, companyId);
    return new ApiResponse<>("Correlativo SKU listado correctamente",
        String.format("%s%07d", type, current + 1));
  }

  @Override
  public String registerSku(String type) {
    Long companyId = TenantContext.getCompanyId();
    int next = repository.registerSku(type, companyId);
    return String.format("%s%07d", type, next);
  }
}
