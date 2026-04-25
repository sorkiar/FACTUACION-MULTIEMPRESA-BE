package com.api.multiempresa.service;

import com.api.multiempresa.dto.request.CompanyRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CompanyResponse;
import java.util.List;

public interface CompanyService {

  ApiResponse<CompanyResponse> create(CompanyRequest request);

  ApiResponse<CompanyResponse> update(Long id, CompanyRequest request);

  ApiResponse<CompanyResponse> findById(Long id);

  ApiResponse<List<CompanyResponse>> findAll();

  ApiResponse<Void> delete(Long id);
}
