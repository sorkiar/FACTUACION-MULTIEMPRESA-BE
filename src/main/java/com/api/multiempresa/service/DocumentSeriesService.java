package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DocumentSeriesResponse;

public interface DocumentSeriesService {

  ApiResponse<DocumentSeriesResponse> getNextSequencePreview(String documentTypeCode);

  ApiResponse<DocumentSeriesResponse> getNextSequenceById(Long seriesId);

}
