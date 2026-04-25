package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.CarrierFilter;
import com.api.multiempresa.dto.request.CarrierRequest;
import com.api.multiempresa.dto.request.CarrierStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CarrierResponse;
import java.util.List;

public interface CarrierService {

  ApiResponse<List<CarrierResponse>> findAll(CarrierFilter filter);

  ApiResponse<CarrierResponse> findById(Long id);

  ApiResponse<CarrierResponse> create(CarrierRequest request);

  ApiResponse<CarrierResponse> update(Long id, CarrierRequest request);

  ApiResponse<Void> updateStatus(Long id, CarrierStatusRequest request);
}
