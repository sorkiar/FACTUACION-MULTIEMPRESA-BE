package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.SalePayment;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SalePaymentRepository extends JpaRepository<SalePayment, Long> {

  @EntityGraph(attributePaths = {
      "paymentMethod",
  })
  List<SalePayment> findBySaleIdAndDeletedAtIsNull(Long saleId);

  @Modifying
  @Query("DELETE FROM SalePayment sp WHERE sp.sale.id = :saleId")
  void deleteBySaleId(Long saleId);
}
