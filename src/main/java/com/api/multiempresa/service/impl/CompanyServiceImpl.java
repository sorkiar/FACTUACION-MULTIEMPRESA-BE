package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.mapper.CompanyMapper;
import com.api.multiempresa.dto.request.CompanyRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CompanyResponse;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.service.CompanyService;
import com.api.multiempresa.util.JwtUtils;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

  private final CompanyRepository companyRepository;
  private final CompanyMapper companyMapper;

  @Override
  @Transactional
  public ApiResponse<CompanyResponse> create(CompanyRequest request) {
    Company company = companyMapper.toEntity(request);
    company.setCreatedBy(JwtUtils.extractUsernameFromContext());
    companyRepository.save(company);
    return new ApiResponse<>("Empresa creada", companyMapper.toResponse(company));
  }

  @Override
  @Transactional
  public ApiResponse<CompanyResponse> update(Long id, CompanyRequest request) {
    Company company = findCompanyOrThrow(id);
    companyMapper.updateEntity(request, company);
    company.setUpdatedBy(JwtUtils.extractUsernameFromContext());
    return new ApiResponse<>("Empresa actualizada", companyMapper.toResponse(company));
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<CompanyResponse> findById(Long id) {
    return new ApiResponse<>("OK", companyMapper.toResponse(findCompanyOrThrow(id)));
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<List<CompanyResponse>> findAll() {
    List<CompanyResponse> list = companyRepository.findByDeletedAtIsNull()
        .stream().map(companyMapper::toResponse).toList();
    return new ApiResponse<>("OK", list);
  }

  @Override
  @Transactional
  public ApiResponse<Void> delete(Long id) {
    Company company = findCompanyOrThrow(id);
    company.setDeletedAt(LocalDateTime.now());
    company.setDeletedBy(JwtUtils.extractUsernameFromContext());
    return new ApiResponse<>("Empresa eliminada", null);
  }

  private Company findCompanyOrThrow(Long id) {
    return companyRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + id));
  }
}
