package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.SaleItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

  @EntityGraph(attributePaths = {
      "product",
      "service",
      "unitMeasure",
  })
  List<SaleItem> findBySaleIdAndDeletedAtIsNull(Long saleId);

  @EntityGraph(attributePaths = {
      "product",
      "service",
      "unitMeasure",
  })
  List<SaleItem> findBySaleId(Long saleId);

  @Modifying
  @Query("DELETE FROM SaleItem si WHERE si.sale.id = :saleId")
  void deleteBySaleId(Long saleId);
}
