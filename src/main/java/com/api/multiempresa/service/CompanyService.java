package com.api.multiempresa.service;

import com.api.multiempresa.dto.request.CompanyRequest;
import com.api.multiempresa.dto.request.CompanyStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CompanyResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyService {

  ApiResponse<CompanyResponse> create(CompanyRequest request, MultipartFile logoFile, MultipartFile pfxFile, String pfxPassword);

  ApiResponse<CompanyResponse> update(Long id, CompanyRequest request, MultipartFile logoFile, MultipartFile pfxFile, String pfxPassword);

  ApiResponse<CompanyResponse> findById(Long id);

  ApiResponse<List<CompanyResponse>> findAll();

  ApiResponse<Void> updateStatus(Long id, CompanyStatusRequest request);
}
