package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.DriverFilter;
import com.api.multiempresa.dto.request.DriverRequest;
import com.api.multiempresa.dto.request.DriverStatusRequest;
import com.api.multiempresa.dto.request.DriverVehicleRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DriverResponse;
import com.api.multiempresa.dto.response.DriverVehicleResponse;
import com.api.multiempresa.service.DriverService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Validated
public class DriverController {

  private final DriverService service;

  @GetMapping
  public ApiResponse<List<DriverResponse>> list(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Integer status
  ) {
    DriverFilter filter = new DriverFilter();
    filter.setId(id);
    filter.setStatus(status);
    return service.findAll(filter);
  }

  @GetMapping("/{id}")
  public ApiResponse<DriverResponse> findById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ApiResponse<DriverResponse> create(@Valid @RequestBody DriverRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public ApiResponse<DriverResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody DriverRequest request
  ) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody DriverStatusRequest request
  ) {
    return service.updateStatus(id, request);
  }

  // ── Vehicle plates ───────────────────────────────────────

  @GetMapping("/{driverId}/vehicles")
  public ApiResponse<List<DriverVehicleResponse>> listVehicles(@PathVariable Long driverId) {
    return service.findVehicles(driverId);
  }

  @PostMapping("/{driverId}/vehicles")
  public ApiResponse<DriverVehicleResponse> addVehicle(
      @PathVariable Long driverId,
      @Valid @RequestBody DriverVehicleRequest request
  ) {
    return service.addVehicle(driverId, request);
  }

  @PutMapping("/{driverId}/vehicles/{vehicleId}")
  public ApiResponse<DriverVehicleResponse> updateVehicle(
      @PathVariable Long driverId,
      @PathVariable Long vehicleId,
      @Valid @RequestBody DriverVehicleRequest request
  ) {
    return service.updateVehicle(driverId, vehicleId, request);
  }

  @DeleteMapping("/{driverId}/vehicles/{vehicleId}")
  public ApiResponse<Void> deleteVehicle(
      @PathVariable Long driverId,
      @PathVariable Long vehicleId
  ) {
    return service.deleteVehicle(driverId, vehicleId);
  }
}
