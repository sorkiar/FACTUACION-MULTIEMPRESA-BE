package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Driver;
import com.api.multiempresa.dto.entity.DriverVehicle;
import com.api.multiempresa.dto.filter.DriverFilter;
import com.api.multiempresa.dto.mapper.DriverMapper;
import com.api.multiempresa.dto.request.DriverRequest;
import com.api.multiempresa.dto.request.DriverStatusRequest;
import com.api.multiempresa.dto.request.DriverVehicleRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DriverResponse;
import com.api.multiempresa.dto.response.DriverVehicleResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.DriverRepository;
import com.api.multiempresa.repository.DriverVehicleRepository;
import com.api.multiempresa.repository.spec.DriverSpecification;
import com.api.multiempresa.service.DriverService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

  private final DriverRepository repository;
  private final DriverVehicleRepository vehicleRepository;
  private final DriverMapper mapper;
  private final CompanyRepository companyRepository;

  @Override
  public ApiResponse<List<DriverResponse>> findAll(DriverFilter filter) {
    List<Driver> drivers = repository.findAll(DriverSpecification.byFilter(filter));
    return new ApiResponse<>("Conductores listados correctamente", mapper.toResponseList(drivers));
  }

  @Override
  public ApiResponse<DriverResponse> findById(Long id) {
    Driver driver = repository.findById(id)
        .filter(d -> !Integer.valueOf(2).equals(d.getStatus()))
        .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));
    return new ApiResponse<>("Conductor obtenido correctamente",
        mapper.toResponseWithVehicles(driver));
  }

  @Override
  public ApiResponse<DriverResponse> create(DriverRequest request) {
    String username = JwtUtils.extractUsernameFromContext();
    Long companyId = TenantContext.getCompanyId();

    String docType = request.getDocType() != null ? request.getDocType() : "DNI";
    if (repository.existsByDocTypeAndDocNumberAndCompany_IdAndStatusNot(
        docType, request.getDocNumber(), companyId, 2)) {
      throw new BusinessValidationException(
          "Ya existe un conductor con ese tipo y número de documento");
    }

    Driver driver = new Driver();
    driver.setDocType(docType);
    driver.setDocNumber(request.getDocNumber());
    driver.setFirstName(request.getFirstName());
    driver.setLastName(request.getLastName());
    driver.setLicenseNumber(request.getLicenseNumber());
    driver.setStatus(1);
    driver.setCreatedBy(username);
    driver.setCompany(companyRepository.getReferenceById(companyId));

    return new ApiResponse<>("Conductor registrado correctamente",
        mapper.toResponseWithVehicles(repository.save(driver)));
  }

  @Override
  public ApiResponse<DriverResponse> update(Long id, DriverRequest request) {
    String username = JwtUtils.extractUsernameFromContext();

    Driver driver = repository.findById(id)
        .filter(d -> !Integer.valueOf(2).equals(d.getStatus()))
        .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

    String docType = request.getDocType() != null ? request.getDocType() : "DNI";
    if (repository.existsByDocTypeAndDocNumberAndCompany_IdAndStatusNotAndIdNot(
        docType, request.getDocNumber(), driver.getCompany().getId(), 2, id)) {
      throw new BusinessValidationException(
          "Ya existe un conductor con ese tipo y número de documento");
    }

    driver.setDocType(docType);
    driver.setDocNumber(request.getDocNumber());
    driver.setFirstName(request.getFirstName());
    driver.setLastName(request.getLastName());
    driver.setLicenseNumber(request.getLicenseNumber());
    driver.setUpdatedBy(username);

    return new ApiResponse<>("Conductor actualizado correctamente",
        mapper.toResponseWithVehicles(repository.save(driver)));
  }

  @Override
  public ApiResponse<Void> updateStatus(Long id, DriverStatusRequest request) {
    String username = JwtUtils.extractUsernameFromContext();

    Driver driver = repository.findById(id)
        .filter(d -> !Integer.valueOf(2).equals(d.getStatus()))
        .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

    driver.setStatus(request.getStatus());
    driver.setUpdatedBy(username);
    repository.save(driver);

    return new ApiResponse<>("Estado actualizado correctamente", null);
  }

  // ── Vehicle plates ───────────────────────────────────────

  @Override
  public ApiResponse<List<DriverVehicleResponse>> findVehicles(Long driverId) {
    requireDriver(driverId);
    List<DriverVehicle> vehicles = vehicleRepository.findByDriverIdAndDeletedAtIsNull(driverId);
    return new ApiResponse<>("Placas listadas correctamente",
        mapper.toVehicleResponseList(vehicles));
  }

  @Override
  public ApiResponse<DriverVehicleResponse> addVehicle(Long driverId, DriverVehicleRequest request) {
    String username = JwtUtils.extractUsernameFromContext();
    Driver driver = requireDriver(driverId);

    DriverVehicle vehicle = new DriverVehicle();
    vehicle.setDriver(driver);
    vehicle.setPlate(request.getPlate().toUpperCase());
    vehicle.setCreatedBy(username);

    return new ApiResponse<>("Placa registrada correctamente",
        mapper.toVehicleResponse(vehicleRepository.save(vehicle)));
  }

  @Override
  public ApiResponse<DriverVehicleResponse> updateVehicle(Long driverId, Long vehicleId,
      DriverVehicleRequest request) {
    String username = JwtUtils.extractUsernameFromContext();

    DriverVehicle vehicle = vehicleRepository
        .findByIdAndDriverIdAndDeletedAtIsNull(vehicleId, driverId)
        .orElseThrow(() -> new ResourceNotFoundException("Placa no encontrada"));

    vehicle.setPlate(request.getPlate().toUpperCase());
    vehicle.setUpdatedBy(username);

    return new ApiResponse<>("Placa actualizada correctamente",
        mapper.toVehicleResponse(vehicleRepository.save(vehicle)));
  }

  @Override
  public ApiResponse<Void> deleteVehicle(Long driverId, Long vehicleId) {
    String username = JwtUtils.extractUsernameFromContext();

    DriverVehicle vehicle = vehicleRepository
        .findByIdAndDriverIdAndDeletedAtIsNull(vehicleId, driverId)
        .orElseThrow(() -> new ResourceNotFoundException("Placa no encontrada"));

    vehicle.setDeletedAt(LocalDateTime.now());
    vehicle.setDeletedBy(username);
    vehicleRepository.save(vehicle);

    return new ApiResponse<>("Placa eliminada correctamente", null);
  }

  private Driver requireDriver(Long driverId) {
    return repository.findById(driverId)
        .filter(d -> !Integer.valueOf(2).equals(d.getStatus()))
        .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));
  }
}
