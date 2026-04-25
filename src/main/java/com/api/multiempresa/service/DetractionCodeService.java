package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.DetractionCodeFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DetractionCodeResponse;
import java.util.List;

public interface DetractionCodeService {
  ApiResponse<List<DetractionCodeResponse>> findAll(DetractionCodeFilter filter);
}
