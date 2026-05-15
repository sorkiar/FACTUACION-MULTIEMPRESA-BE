package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.DocumentSeries;
import com.api.multiempresa.dto.entity.DocumentTypeSunat;
import com.api.multiempresa.dto.mapper.DocumentSeriesMapper;
import com.api.multiempresa.dto.request.DocumentSeriesCreateRequest;
import com.api.multiempresa.dto.request.DocumentSeriesUpdateRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DocumentSeriesResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.DocumentSeriesRepository;
import com.api.multiempresa.repository.DocumentTypeSunatRepository;
import com.api.multiempresa.service.DocumentSeriesService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentSeriesServiceImpl implements DocumentSeriesService {

  private static final int MAX_ACTIVE_SERIES = 1;
  private static final java.util.Set<String> NC_ND_CODES = java.util.Set.of("07", "08");
  private static final java.util.Set<String> VALID_PARENT_CODES = java.util.Set.of("01", "03");
  private static final int STATUS_ACTIVE = 1;
  private static final int STATUS_INACTIVE = 0;

  private final DocumentSeriesRepository repository;
  private final DocumentTypeSunatRepository documentTypeSunatRepository;
  private final DocumentSeriesMapper mapper;
  private final CompanyRepository companyRepository;

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<List<DocumentSeriesResponse>> findAll() {
    Long companyId = TenantContext.getCompanyId();
    List<DocumentSeries> series = repository
        .findByCompany_IdOrderByDocumentTypeSunat_CodeAscSeriesAsc(companyId);
    return new ApiResponse<>("Series listadas correctamente",
        series.stream().map(mapper::toResponse).toList());
  }

  @Override
  @Transactional
  public ApiResponse<DocumentSeriesResponse> create(DocumentSeriesCreateRequest request) {
    Long companyId = TenantContext.getCompanyId();
    String username = JwtUtils.extractUsernameFromContext();
    String typeCode = request.getDocumentTypeCode().trim().toUpperCase();

    DocumentTypeSunat documentType = documentTypeSunatRepository
        .findByCodeAndStatusNot(typeCode, 2)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Tipo de documento no encontrado: " + typeCode));

    String parentCode = resolveAndValidateParentCode(typeCode, request.getParentDocumentTypeCode());

    long activeSeries = repository.countByCompany_IdAndDocumentTypeSunat_CodeAndParentDocumentTypeCodeAndStatus(
        companyId, typeCode, parentCode, STATUS_ACTIVE);
    if (activeSeries >= MAX_ACTIVE_SERIES) {
      throw new BusinessValidationException(
          "Ya existe una serie activa para el tipo de documento " + typeCode
              + (parentCode != null ? " con tipo padre " + parentCode : "")
              + ". Inactiva la serie existente antes de crear una nueva.");
    }

    DocumentSeries entity = new DocumentSeries();
    entity.setCompany(companyRepository.getReferenceById(companyId));
    entity.setDocumentTypeSunat(documentType);
    entity.setParentDocumentTypeCode(parentCode);
    entity.setSeries(request.getSeries().trim().toUpperCase());
    entity.setCurrentSequence(0);
    entity.setStatus(STATUS_ACTIVE);
    entity.setCreatedBy(username);

    return new ApiResponse<>("Serie creada correctamente", mapper.toResponse(repository.save(entity)));
  }

  @Override
  @Transactional
  public ApiResponse<DocumentSeriesResponse> update(Long id, DocumentSeriesUpdateRequest request) {
    Long companyId = TenantContext.getCompanyId();
    String username = JwtUtils.extractUsernameFromContext();

    DocumentSeries entity = repository.findByIdAndCompany_Id(id, companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Serie no encontrada con id: " + id));

    if (entity.getCurrentSequence() > 0) {
      throw new BusinessValidationException(
          "No se puede modificar la serie porque ya tiene comprobantes emitidos (correlativo: "
              + entity.getCurrentSequence() + ").");
    }

    entity.setSeries(request.getSeries().trim().toUpperCase());
    entity.setUpdatedBy(username);

    return new ApiResponse<>("Serie actualizada correctamente", mapper.toResponse(repository.save(entity)));
  }

  @Override
  @Transactional
  public ApiResponse<DocumentSeriesResponse> toggleStatus(Long id) {
    Long companyId = TenantContext.getCompanyId();
    String username = JwtUtils.extractUsernameFromContext();

    DocumentSeries entity = repository.findByIdAndCompany_Id(id, companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Serie no encontrada con id: " + id));

    if (entity.getStatus() == STATUS_ACTIVE) {
      entity.setStatus(STATUS_INACTIVE);
    } else if (entity.getStatus() == STATUS_INACTIVE) {
      String typeCode = entity.getDocumentTypeSunat().getCode();
      String parentCode = entity.getParentDocumentTypeCode();
      long activeSeries = repository.countByCompany_IdAndDocumentTypeSunat_CodeAndParentDocumentTypeCodeAndStatus(
          companyId, typeCode, parentCode, STATUS_ACTIVE);
      if (activeSeries >= MAX_ACTIVE_SERIES) {
        throw new BusinessValidationException(
            "Ya existe una serie activa para el tipo de documento " + typeCode
                + (parentCode != null ? " con tipo padre " + parentCode : "")
                + ". Inactiva la serie existente antes de activar esta.");
      }
      entity.setStatus(STATUS_ACTIVE);
    } else {
      throw new BusinessValidationException("Esta serie no puede ser activada.");
    }

    entity.setUpdatedBy(username);
    String msg = entity.getStatus() == STATUS_ACTIVE ? "Serie activada correctamente" : "Serie inactivada correctamente";
    return new ApiResponse<>(msg, mapper.toResponse(repository.save(entity)));
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<DocumentSeriesResponse> getNextSequencePreview(String documentTypeCode) {
    Long companyId = TenantContext.getCompanyId();

    DocumentSeries series = (companyId != null)
        ? repository.findFirstByDocumentTypeSunat_CodeAndStatusAndCompany_IdOrderByIdAsc(
            documentTypeCode, STATUS_ACTIVE, companyId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe serie activa para el tipo de documento: " + documentTypeCode))
        : repository.findFirstByDocumentTypeSunat_CodeAndStatusNotOrderByIdAsc(documentTypeCode, 2)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe serie activa para el tipo de documento: " + documentTypeCode));

    DocumentSeriesResponse response = mapper.toResponse(series);
    response.setSequence(series.getCurrentSequence() + 1);
    return new ApiResponse<>("Correlativo obtenido correctamente", response);
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<DocumentSeriesResponse> getNextSequenceById(Long seriesId) {
    Long companyId = TenantContext.getCompanyId();

    DocumentSeries series = (companyId != null
        ? repository.findByIdAndCompany_Id(seriesId, companyId)
        : repository.findById(seriesId))
        .orElseThrow(() -> new ResourceNotFoundException("No existe serie con id: " + seriesId));

    DocumentSeriesResponse response = mapper.toResponse(series);
    response.setSequence(series.getCurrentSequence() + 1);
    return new ApiResponse<>("Correlativo obtenido correctamente", response);
  }

  private String resolveAndValidateParentCode(String typeCode, String rawParentCode) {
    boolean isNcNd = NC_ND_CODES.contains(typeCode);
    if (isNcNd) {
      if (rawParentCode == null || rawParentCode.isBlank()) {
        throw new BusinessValidationException(
            "Para el tipo de documento " + typeCode
                + " se debe indicar el parentDocumentTypeCode (01=Factura, 03=Boleta).");
      }
      String parent = rawParentCode.trim();
      if (!VALID_PARENT_CODES.contains(parent)) {
        throw new BusinessValidationException(
            "parentDocumentTypeCode inválido: '" + parent + "'. Valores permitidos: 01, 03.");
      }
      return parent;
    }
    if (rawParentCode != null && !rawParentCode.isBlank()) {
      throw new BusinessValidationException(
          "parentDocumentTypeCode solo aplica para tipos 07 y 08.");
    }
    return null;
  }
}
