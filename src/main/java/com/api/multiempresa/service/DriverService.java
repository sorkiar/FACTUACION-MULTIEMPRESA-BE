package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.DriverFilter;
import com.api.multiempresa.dto.request.DriverRequest;
import com.api.multiempresa.dto.request.DriverStatusRequest;
import com.api.multiempresa.dto.request.DriverVehicleRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DriverResponse;
import com.api.multiempresa.dto.response.DriverVehicleResponse;
import java.util.List;

public interface DriverService {

  ApiResponse<List<DriverResponse>> findAll(DriverFilter filter);

  ApiResponse<DriverResponse> findById(Long id);

  ApiResponse<DriverResponse> create(DriverRequest request);

  ApiResponse<DriverResponse> update(Long id, DriverRequest request);

  ApiResponse<Void> updateStatus(Long id, DriverStatusRequest request);

  // ── Vehicle plate endpoints ──────────────────────────────

  ApiResponse<List<DriverVehicleResponse>> findVehicles(Long driverId);

  ApiResponse<DriverVehicleResponse> addVehicle(Long driverId, DriverVehicleRequest request);

  ApiResponse<DriverVehicleResponse> updateVehicle(Long driverId, Long vehicleId,
      DriverVehicleRequest request);

  ApiResponse<Void> deleteVehicle(Long driverId, Long vehicleId);
}
