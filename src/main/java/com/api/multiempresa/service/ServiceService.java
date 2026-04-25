package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.ServiceFilter;
import com.api.multiempresa.dto.request.ServiceRequest;
import com.api.multiempresa.dto.request.ServiceStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ServiceResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ServiceService {
  ApiResponse<List<ServiceResponse>> findAll(ServiceFilter filter);

  ApiResponse<ServiceResponse> create(ServiceRequest request, MultipartFile image, MultipartFile technicalSheet);

  ApiResponse<ServiceResponse> update(Long id, ServiceRequest request, MultipartFile image, MultipartFile technicalSheet);

  ApiResponse<Void> updateStatus(Long id, ServiceStatusRequest request);

  byte[] generatePdf(Long id);
}
