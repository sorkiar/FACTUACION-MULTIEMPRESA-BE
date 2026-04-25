package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.DocumentSeries;
import com.api.multiempresa.dto.mapper.DocumentSeriesMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DocumentSeriesResponse;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.DocumentSeriesRepository;
import com.api.multiempresa.service.DocumentSeriesService;
import com.api.multiempresa.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentSeriesServiceImpl implements DocumentSeriesService {

  private final DocumentSeriesRepository repository;
  private final DocumentSeriesMapper mapper;
  private final CompanyRepository companyRepository;

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<DocumentSeriesResponse> getNextSequencePreview(
      String documentTypeCode
  ) {
    Long companyId = TenantContext.getCompanyId();

    DocumentSeries series = (companyId != null)
        ? repository.findFirstByDocumentTypeSunat_CodeAndStatusNotAndCompanyIdOrderByIdAsc(
            documentTypeCode, 2, companyId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe serie activa para el tipo de documento: " + documentTypeCode))
        : repository.findFirstByDocumentTypeSunat_CodeAndStatusNotOrderByIdAsc(documentTypeCode, 2)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No existe serie activa para el tipo de documento: " + documentTypeCode));

    DocumentSeriesResponse response = mapper.toResponse(series);

    response.setSequence(series.getCurrentSequence() + 1);

    return new ApiResponse<>(
        "Correlativo obtenido correctamente",
        response
    );
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<DocumentSeriesResponse> getNextSequenceById(Long seriesId) {
    DocumentSeries series = repository.findById(seriesId)
        .orElseThrow(() ->
            new ResourceNotFoundException("No existe serie con id: " + seriesId)
        );

    DocumentSeriesResponse response = mapper.toResponse(series);
    response.setSequence(series.getCurrentSequence() + 1);

    return new ApiResponse<>("Correlativo obtenido correctamente", response);
  }
}
