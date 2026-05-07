package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.ServiceCategory;
import com.api.multiempresa.dto.filter.ServiceCategoryFilter;
import com.api.multiempresa.dto.mapper.ServiceCategoryMapper;
import com.api.multiempresa.dto.request.ServiceCategoryRequest;
import com.api.multiempresa.dto.request.ServiceCategoryStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ServiceCategoryResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.ServiceCategoryRepository;
import com.api.multiempresa.repository.spec.ServiceCategorySpecification;
import com.api.multiempresa.service.ServiceCategoryService;
import com.api.multiempresa.util.JwtUtils;
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
        mapper.toResponseList(repository.findAll(ServiceCategorySpecification.byFilter(filter)))
    );
  }

  @Override
  public ApiResponse<ServiceCategoryResponse> findById(Long id) {
    return new ApiResponse<>("Categoría de servicio obtenida correctamente",
        mapper.toResponse(findOrThrow(id)));
  }

  @Override
  public ApiResponse<ServiceCategoryResponse> create(ServiceCategoryRequest request) {
    Long companyId = TenantContext.getCompanyId();

    if (repository.existsByNameAndCompany_IdAndDeletedAtIsNull(request.getName(), companyId)) {
      throw new BusinessValidationException("Ya existe una categoría de servicio con ese nombre");
    }

    ServiceCategory category = new ServiceCategory();
    category.setName(request.getName());
    category.setDescription(request.getDescription());
    category.setStatus(1);
    category.setCreatedBy(JwtUtils.extractUsernameFromContext());
    category.setCompany(companyRepository.getReferenceById(companyId));

    return new ApiResponse<>("Categoría de servicio registrada correctamente",
        mapper.toResponse(repository.save(category)));
  }

  @Override
  public ApiResponse<ServiceCategoryResponse> update(Long id, ServiceCategoryRequest request) {
    Long companyId = TenantContext.getCompanyId();
    ServiceCategory category = findOrThrow(id);

    if (repository.existsByNameAndCompany_IdAndDeletedAtIsNullAndIdNot(
        request.getName(), companyId, id)) {
      throw new BusinessValidationException("Ya existe una categoría de servicio con ese nombre");
    }

    category.setName(request.getName());
    category.setDescription(request.getDescription());
    category.setUpdatedBy(JwtUtils.extractUsernameFromContext());

    return new ApiResponse<>("Categoría de servicio actualizada correctamente",
        mapper.toResponse(repository.save(category)));
  }

  @Override
  public ApiResponse<Void> updateStatus(Long id, ServiceCategoryStatusRequest request) {
    ServiceCategory category = findOrThrow(id);
    category.setStatus(request.getStatus());
    category.setUpdatedBy(JwtUtils.extractUsernameFromContext());
    repository.save(category);
    return new ApiResponse<>("Estado actualizado correctamente", null);
  }

  private ServiceCategory findOrThrow(Long id) {
    return repository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría de servicio no encontrada"));
  }
}
