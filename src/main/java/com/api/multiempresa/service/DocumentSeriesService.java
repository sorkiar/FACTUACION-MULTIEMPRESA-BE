package com.api.multiempresa.service;

import com.api.multiempresa.dto.request.DocumentSeriesCreateRequest;
import com.api.multiempresa.dto.request.DocumentSeriesUpdateRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DocumentSeriesResponse;
import java.util.List;

public interface DocumentSeriesService {

  ApiResponse<List<DocumentSeriesResponse>> findAll();

  ApiResponse<DocumentSeriesResponse> create(DocumentSeriesCreateRequest request);

  ApiResponse<DocumentSeriesResponse> update(Long id, DocumentSeriesUpdateRequest request);

  ApiResponse<DocumentSeriesResponse> toggleStatus(Long id);

  ApiResponse<DocumentSeriesResponse> getNextSequencePreview(String documentTypeCode);

  ApiResponse<DocumentSeriesResponse> getNextSequenceById(Long seriesId);

}
