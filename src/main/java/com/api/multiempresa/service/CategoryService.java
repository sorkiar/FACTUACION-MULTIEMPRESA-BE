package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.CategoryFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {
  ApiResponse<List<CategoryResponse>> findAll(CategoryFilter filter);
}
