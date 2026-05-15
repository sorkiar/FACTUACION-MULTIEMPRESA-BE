package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.RemissionGuide;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RemissionGuideRepository
    extends JpaRepository<RemissionGuide, Long>,
    JpaSpecificationExecutor<RemissionGuide> {

  @Override
  @EntityGraph(attributePaths = {
      "documentSeries", "documentSeries.documentTypeSunat",
      "client", "client.documentType", "client.personType",
      "clientAddress",
      "carrier",
      "items", "items.product",
      "drivers", "drivers.driver", "drivers.driverVehicle",
  })
  @NullMarked
  List<RemissionGuide> findAll(Specification<RemissionGuide> spec);

  @EntityGraph(attributePaths = {
      "documentSeries", "documentSeries.documentTypeSunat",
      "client", "client.documentType", "client.personType",
      "clientAddress",
      "carrier",
      "items", "items.product",
      "drivers", "drivers.driver", "drivers.driverVehicle",
  })
  Optional<RemissionGuide> findByIdAndDeletedAtIsNull(Long id);

  @EntityGraph(attributePaths = {
      "documentSeries", "documentSeries.documentTypeSunat",
      "client", "client.documentType", "client.personType",
      "clientAddress",
      "carrier",
      "items", "items.product",
      "drivers", "drivers.driver", "drivers.driverVehicle",
  })
  Optional<RemissionGuide> findByIdAndCompany_IdAndDeletedAtIsNull(Long id, Long companyId);

  // Job accede a guide.getClient(), guide.getCarrier(), guide.getDrivers().driver/vehicle
  @EntityGraph(attributePaths = {
      "client", "client.documentType", "client.personType",
      "carrier",
      "drivers", "drivers.driver", "drivers.driverVehicle",
  })
  List<RemissionGuide> findByStatusAndDeletedAtIsNull(String status);

  @EntityGraph(attributePaths = {})
  List<RemissionGuide> findByDeletedAtIsNullOrderByIssueDateDesc();

  @EntityGraph(attributePaths = {})
  List<RemissionGuide> findByStatusAndDeletedAtIsNullOrderByIssueDateDesc(String status);

  @EntityGraph(attributePaths = {})
  List<RemissionGuide> findByCompany_IdAndDeletedAtIsNullOrderByIssueDateDesc(Long companyId);

  @EntityGraph(attributePaths = {})
  List<RemissionGuide> findByCompany_IdAndStatusAndDeletedAtIsNullOrderByIssueDateDesc(Long companyId, String status);
}
