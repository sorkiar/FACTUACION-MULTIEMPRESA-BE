package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Service;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceRepository
    extends JpaRepository<Service, Long>,
    JpaSpecificationExecutor<Service> {

  @Override
  @EntityGraph(attributePaths = {
      "serviceCategory",
      "chargeUnit",
      "detractionCode",
  })
  @NullMarked
  List<Service> findAll(Specification<Service> spec);

  boolean existsBySku(String sku);

  @EntityGraph(attributePaths = {
      "serviceCategory",
      "chargeUnit",
      "detractionCode",
  })
  Optional<Service> findByIdAndStatusNot(Long id, Integer status);

  @EntityGraph(attributePaths = {
      "serviceCategory",
      "chargeUnit",
      "detractionCode",
  })
  Optional<Service> findByIdAndCompany_IdAndStatusNot(Long id, Long companyId, Integer status);
}
