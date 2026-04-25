package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Ubigeo;
import com.api.multiempresa.dto.filter.UbigeoFilter;
import com.api.multiempresa.dto.mapper.UbigeoMapper;
import com.api.multiempresa.dto.request.UbigeoRequest;
import com.api.multiempresa.dto.request.UbigeoStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UbigeoResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.UbigeoRepository;
import com.api.multiempresa.repository.spec.UbigeoSpecification;
import com.api.multiempresa.service.UbigeoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UbigeoServiceImpl implements UbigeoService {

  private final UbigeoRepository ubigeoRepository;
  private final UbigeoMapper ubigeoMapper;

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<List<UbigeoResponse>> findAll(UbigeoFilter filter) {
    Specification<Ubigeo> spec = UbigeoSpecification.byFilter(filter);
    List<Ubigeo> entities = ubigeoRepository.findAll(spec);
    return new ApiResponse<>(null, ubigeoMapper.toResponseList(entities));
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<UbigeoResponse> findById(String ubigeo) {
    Ubigeo entity = findOrThrow(ubigeo);
    return new ApiResponse<>(null, ubigeoMapper.toResponse(entity));
  }

  @Override
  @Transactional
  public ApiResponse<UbigeoResponse> create(UbigeoRequest request) {
    if (ubigeoRepository.existsById(request.getUbigeo())) {
      throw new BusinessValidationException(
          "El código de ubigeo ya existe: " + request.getUbigeo());
    }
    Ubigeo entity = ubigeoMapper.toEntity(request);
    entity.setStatus(1);
    return new ApiResponse<>("Ubigeo creado exitosamente.",
        ubigeoMapper.toResponse(ubigeoRepository.save(entity)));
  }

  @Override
  @Transactional
  public ApiResponse<UbigeoResponse> update(String ubigeo, UbigeoRequest request) {
    Ubigeo entity = findOrThrow(ubigeo);
    ubigeoMapper.updateEntity(entity, request);
    return new ApiResponse<>("Ubigeo actualizado exitosamente.",
        ubigeoMapper.toResponse(ubigeoRepository.save(entity)));
  }

  @Override
  @Transactional
  public ApiResponse<Void> updateStatus(String ubigeo, UbigeoStatusRequest request) {
    Ubigeo entity = findOrThrow(ubigeo);
    entity.setStatus(request.getStatus());
    ubigeoRepository.save(entity);
    return new ApiResponse<>("Estado actualizado.", null);
  }

  private Ubigeo findOrThrow(String ubigeo) {
    return ubigeoRepository.findById(ubigeo)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Ubigeo no encontrado: " + ubigeo));
  }
}
