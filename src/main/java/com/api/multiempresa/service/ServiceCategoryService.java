package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.ServiceCategoryFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ServiceCategoryResponse;
import java.util.List;

public interface ServiceCategoryService {
  ApiResponse<List<ServiceCategoryResponse>> findAll(ServiceCategoryFilter filter);
}
