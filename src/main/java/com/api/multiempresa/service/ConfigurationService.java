package com.api.multiempresa.service;

import com.api.multiempresa.dto.request.ConfigurationRequest;
import com.api.multiempresa.dto.request.ConfigurationUpdateRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ConfigurationResponse;
import java.util.List;
import java.util.Map;

public interface ConfigurationService {

  Map<String, String> getGroup(String group);

  ApiResponse<List<ConfigurationResponse>> findEditable();

  ApiResponse<ConfigurationResponse> create(ConfigurationRequest request);

  ApiResponse<ConfigurationResponse> update(Long id, ConfigurationUpdateRequest request);
}
