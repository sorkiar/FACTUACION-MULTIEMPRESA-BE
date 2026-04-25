package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.filter.ServiceCategoryFilter;
import com.api.multiempresa.dto.mapper.ServiceCategoryMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ServiceCategoryResponse;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.ServiceCategoryRepository;
import com.api.multiempresa.repository.spec.ServiceCategorySpecification;
import com.api.multiempresa.service.ServiceCategoryService;
import com.api.multiempresa.util.TenantContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceCategoryServiceImpl implements ServiceCategoryService {
  private final ServiceCategoryRepository repository;
  private final ServiceCategoryMapper mapper;
  private final CompanyRepository companyRepository;

  @Override
  public ApiResponse<List<ServiceCategoryResponse>> findAll(ServiceCategoryFilter filter) {
    return new ApiResponse<>(
        "Categorías de servicio listadas correctamente",
        mapper.toResponseList(
            repository.findAll(ServiceCategorySpecification.byFilter(filter))
        )
    );
  }
}
