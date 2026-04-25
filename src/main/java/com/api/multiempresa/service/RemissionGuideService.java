package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.RemissionGuideFilter;
import com.api.multiempresa.dto.request.RemissionGuideRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.RemissionGuideResponse;
import java.util.List;

public interface RemissionGuideService {

  ApiResponse<List<RemissionGuideResponse>> findAll(RemissionGuideFilter filter);

  ApiResponse<RemissionGuideResponse> findById(Long id);

  ApiResponse<RemissionGuideResponse> create(RemissionGuideRequest request);
}
