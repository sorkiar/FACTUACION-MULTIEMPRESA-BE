package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Carrier;
import com.api.multiempresa.dto.filter.CarrierFilter;
import com.api.multiempresa.dto.mapper.CarrierMapper;
import com.api.multiempresa.dto.request.CarrierRequest;
import com.api.multiempresa.dto.request.CarrierStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CarrierResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CarrierRepository;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.spec.CarrierSpecification;
import com.api.multiempresa.service.CarrierService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarrierServiceImpl implements CarrierService {

  private final CarrierRepository repository;
  private final CarrierMapper mapper;
  private final CompanyRepository companyRepository;

  @Override
  public ApiResponse<List<CarrierResponse>> findAll(CarrierFilter filter) {
    List<Carrier> carriers = repository.findAll(CarrierSpecification.byFilter(filter));
    return new ApiResponse<>("Transportistas listados correctamente", mapper.toResponseList(carriers));
  }

  @Override
  public ApiResponse<CarrierResponse> findById(Long id) {
    return new ApiResponse<>("Transportista obtenido correctamente", mapper.toResponse(findOrThrow(id)));
  }

  @Override
  public ApiResponse<CarrierResponse> create(CarrierRequest request) {
    String username = JwtUtils.extractUsernameFromContext();
    Long companyId = TenantContext.getCompanyId();

    String docType = request.getDocType() != null ? request.getDocType() : "RUC";
    if (repository.existsByDocTypeAndDocNumberAndCompany_IdAndStatusNot(
        docType, request.getDocNumber(), companyId, 2)) {
      throw new BusinessValidationException(
          "Ya existe un transportista con ese tipo y número de documento");
    }

    Carrier carrier = new Carrier();
    carrier.setDocType(docType);
    carrier.setDocNumber(request.getDocNumber());
    carrier.setBusinessName(request.getBusinessName());
    carrier.setStatus(1);
    carrier.setCreatedBy(username);
    carrier.setCompany(companyRepository.getReferenceById(companyId));

    return new ApiResponse<>("Transportista registrado correctamente",
        mapper.toResponse(repository.save(carrier)));
  }

  @Override
  public ApiResponse<CarrierResponse> update(Long id, CarrierRequest request) {
    String username = JwtUtils.extractUsernameFromContext();

    Carrier carrier = findOrThrow(id);

    String docType = request.getDocType() != null ? request.getDocType() : "RUC";
    if (repository.existsByDocTypeAndDocNumberAndCompany_IdAndStatusNotAndIdNot(
        docType, request.getDocNumber(), carrier.getCompany().getId(), 2, id)) {
      throw new BusinessValidationException(
          "Ya existe un transportista con ese tipo y número de documento");
    }

    carrier.setDocType(docType);
    carrier.setDocNumber(request.getDocNumber());
    carrier.setBusinessName(request.getBusinessName());
    carrier.setUpdatedBy(username);

    return new ApiResponse<>("Transportista actualizado correctamente",
        mapper.toResponse(repository.save(carrier)));
  }

  @Override
  public ApiResponse<Void> updateStatus(Long id, CarrierStatusRequest request) {
    String username = JwtUtils.extractUsernameFromContext();

    Carrier carrier = findOrThrow(id);

    carrier.setStatus(request.getStatus());
    carrier.setUpdatedBy(username);
    repository.save(carrier);

    return new ApiResponse<>("Estado actualizado correctamente", null);
  }

  private Carrier findOrThrow(Long id) {
    Long companyId = TenantContext.getCurrentCompanyId();
    return repository.findByIdAndCompany_IdAndStatusNot(id, companyId, 2)
        .orElseThrow(() -> new ResourceNotFoundException("Transportista no encontrado"));
  }
}
