package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.filter.CategoryFilter;
import com.api.multiempresa.dto.mapper.CategoryMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CategoryResponse;
import com.api.multiempresa.repository.CategoryRepository;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.spec.CategorySpecification;
import com.api.multiempresa.service.CategoryService;
import com.api.multiempresa.util.TenantContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository repository;
  private final CategoryMapper mapper;
  private final CompanyRepository companyRepository;

  @Override
  public ApiResponse<List<CategoryResponse>> findAll(CategoryFilter filter) {
    return new ApiResponse<>(
        "Categorías listadas correctamente",
        mapper.toResponseList(
            repository.findAll(CategorySpecification.byFilter(filter))
        )
    );
  }
}
