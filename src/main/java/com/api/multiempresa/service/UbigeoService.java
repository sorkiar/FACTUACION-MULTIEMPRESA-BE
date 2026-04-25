package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.UbigeoFilter;
import com.api.multiempresa.dto.request.UbigeoRequest;
import com.api.multiempresa.dto.request.UbigeoStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UbigeoResponse;
import java.util.List;

public interface UbigeoService {

  ApiResponse<List<UbigeoResponse>> findAll(UbigeoFilter filter);

  ApiResponse<UbigeoResponse> findById(String ubigeo);

  ApiResponse<UbigeoResponse> create(UbigeoRequest request);

  ApiResponse<UbigeoResponse> update(String ubigeo, UbigeoRequest request);

  ApiResponse<Void> updateStatus(String ubigeo, UbigeoStatusRequest request);
}
