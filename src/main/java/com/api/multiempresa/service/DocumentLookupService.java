package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DniRecordResponse;
import com.api.multiempresa.dto.response.RucRecordResponse;

public interface DocumentLookupService {

  ApiResponse<DniRecordResponse> queryDni(String dni);

  ApiResponse<RucRecordResponse> queryRuc(String ruc);
}
