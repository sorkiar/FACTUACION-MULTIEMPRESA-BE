package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.ApiResponse;

public interface SkuSequenceService {
  ApiResponse<String> generateSkuPreview(String type);

  String registerSku(String type);
}
