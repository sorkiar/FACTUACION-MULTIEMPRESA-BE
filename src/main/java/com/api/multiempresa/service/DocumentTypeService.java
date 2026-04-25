package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.DocumentTypeFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DocumentTypeResponse;
import java.util.List;

public interface DocumentTypeService {
  ApiResponse<List<DocumentTypeResponse>> findAll(DocumentTypeFilter filter);
}
