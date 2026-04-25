package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.ProductFilter;
import com.api.multiempresa.dto.request.ProductRequest;
import com.api.multiempresa.dto.request.ProductStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ProductResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
  ApiResponse<List<ProductResponse>> findAll(ProductFilter filter);

  ApiResponse<ProductResponse> create(ProductRequest request, MultipartFile mainImage, MultipartFile technicalSheet);

  ApiResponse<ProductResponse> update(Long id, ProductRequest request, MultipartFile mainImage, MultipartFile technicalSheet);

  ApiResponse<Void> updateStatus(Long id, ProductStatusRequest request);
}
