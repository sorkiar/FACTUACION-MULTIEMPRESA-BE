package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.SaleInstallment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SaleInstallmentRepository extends JpaRepository<SaleInstallment, Long> {

  List<SaleInstallment> findBySaleIdAndDeletedAtIsNullOrderByInstallmentNumberAsc(Long saleId);

  @Modifying
  @Query("DELETE FROM SaleInstallment i WHERE i.sale.id = :saleId")
  void deleteBySaleId(Long saleId);
}
