package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.DriverVehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverVehicleRepository extends JpaRepository<DriverVehicle, Long> {

  List<DriverVehicle> findByDriverIdAndDeletedAtIsNull(Long driverId);

  Optional<DriverVehicle> findByIdAndDriverIdAndDeletedAtIsNull(Long id, Long driverId);
}
