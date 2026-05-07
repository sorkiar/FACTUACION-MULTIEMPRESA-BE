package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Category;
import com.api.multiempresa.dto.filter.CategoryFilter;
import com.api.multiempresa.dto.mapper.CategoryMapper;
import com.api.multiempresa.dto.request.CategoryRequest;
import com.api.multiempresa.dto.request.CategoryStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CategoryResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CategoryRepository;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.spec.CategorySpecification;
import com.api.multiempresa.service.CategoryService;
import com.api.multiempresa.util.JwtUtils;
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
        mapper.toResponseList(repository.findAll(CategorySpecification.byFilter(filter)))
    );
  }

  @Override
  public ApiResponse<CategoryResponse> findById(Long id) {
    return new ApiResponse<>("Categoría obtenida correctamente",
        mapper.toResponse(findOrThrow(id)));
  }

  @Override
  public ApiResponse<CategoryResponse> create(CategoryRequest request) {
    Long companyId = TenantContext.getCompanyId();

    if (repository.existsByNameAndCompany_IdAndDeletedAtIsNull(request.getName(), companyId)) {
      throw new BusinessValidationException("Ya existe una categoría con ese nombre");
    }

    Category category = new Category();
    category.setName(request.getName());
    category.setDescription(request.getDescription());
    category.setStatus(1);
    category.setCreatedBy(JwtUtils.extractUsernameFromContext());
    category.setCompany(companyRepository.getReferenceById(companyId));

    return new ApiResponse<>("Categoría registrada correctamente",
        mapper.toResponse(repository.save(category)));
  }

  @Override
  public ApiResponse<CategoryResponse> update(Long id, CategoryRequest request) {
    Long companyId = TenantContext.getCompanyId();
    Category category = findOrThrow(id);

    if (repository.existsByNameAndCompany_IdAndDeletedAtIsNullAndIdNot(
        request.getName(), companyId, id)) {
      throw new BusinessValidationException("Ya existe una categoría con ese nombre");
    }

    category.setName(request.getName());
    category.setDescription(request.getDescription());
    category.setUpdatedBy(JwtUtils.extractUsernameFromContext());

    return new ApiResponse<>("Categoría actualizada correctamente",
        mapper.toResponse(repository.save(category)));
  }

  @Override
  public ApiResponse<Void> updateStatus(Long id, CategoryStatusRequest request) {
    Category category = findOrThrow(id);
    category.setStatus(request.getStatus());
    category.setUpdatedBy(JwtUtils.extractUsernameFromContext());
    repository.save(category);
    return new ApiResponse<>("Estado actualizado correctamente", null);
  }

  private Category findOrThrow(Long id) {
    return repository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
  }
}
