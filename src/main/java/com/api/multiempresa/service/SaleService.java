package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.SaleFilter;
import com.api.multiempresa.dto.request.SaleRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.SaleResponse;
import java.util.List;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

public interface SaleService {

  ApiResponse<List<SaleResponse>> findAll(SaleFilter filter);

  ApiResponse<SaleResponse> create(SaleRequest request, MultiValueMap<String, MultipartFile> paymentProofs);

  ApiResponse<SaleResponse> updateDraft(Long id, SaleRequest request, MultiValueMap<String, MultipartFile> paymentProofs);

  byte[] generateQuotation(SaleRequest request);

}
