package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.CategoryFilter;
import com.api.multiempresa.dto.request.CategoryRequest;
import com.api.multiempresa.dto.request.CategoryStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {

  ApiResponse<List<CategoryResponse>> findAll(CategoryFilter filter);

  ApiResponse<CategoryResponse> findById(Long id);

  ApiResponse<CategoryResponse> create(CategoryRequest request);

  ApiResponse<CategoryResponse> update(Long id, CategoryRequest request);

  ApiResponse<Void> updateStatus(Long id, CategoryStatusRequest request);
}
