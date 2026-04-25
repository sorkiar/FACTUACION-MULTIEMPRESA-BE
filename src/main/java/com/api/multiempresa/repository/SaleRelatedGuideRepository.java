package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.SaleRelatedGuide;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SaleRelatedGuideRepository extends JpaRepository<SaleRelatedGuide, Long> {

  List<SaleRelatedGuide> findBySaleId(Long saleId);

  @Modifying
  @Query("DELETE FROM SaleRelatedGuide g WHERE g.sale.id = :saleId")
  void deleteBySaleId(Long saleId);
}
