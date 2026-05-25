package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Product;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository
    extends JpaRepository<Product, Long>,
    JpaSpecificationExecutor<Product> {

  @Override
  @EntityGraph(attributePaths = {
      "category",
      "unitMeasure",
      "detractionCode",
  })
  @NullMarked
  List<Product> findAll(Specification<Product> spec);

  boolean existsBySkuAndCompanyId(String sku, Long companyId);

  @EntityGraph(attributePaths = {
      "category",
      "unitMeasure",
      "detractionCode",
  })
  Optional<Product> findByIdAndStatusNot(Long id, Integer status);

  @EntityGraph(attributePaths = {
      "category",
      "unitMeasure",
      "detractionCode",
  })
  Optional<Product> findByIdAndCompany_IdAndStatusNot(Long id, Long companyId, Integer status);
}
