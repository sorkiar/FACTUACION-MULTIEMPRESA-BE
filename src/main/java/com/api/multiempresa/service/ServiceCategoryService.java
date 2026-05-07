package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.ServiceCategoryFilter;
import com.api.multiempresa.dto.request.ServiceCategoryRequest;
import com.api.multiempresa.dto.request.ServiceCategoryStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ServiceCategoryResponse;
import java.util.List;

public interface ServiceCategoryService {

  ApiResponse<List<ServiceCategoryResponse>> findAll(ServiceCategoryFilter filter);

  ApiResponse<ServiceCategoryResponse> findById(Long id);

  ApiResponse<ServiceCategoryResponse> create(ServiceCategoryRequest request);

  ApiResponse<ServiceCategoryResponse> update(Long id, ServiceCategoryRequest request);

  ApiResponse<Void> updateStatus(Long id, ServiceCategoryStatusRequest request);
}
