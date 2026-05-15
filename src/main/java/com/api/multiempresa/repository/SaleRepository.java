package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Sale;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleRepository
    extends JpaRepository<Sale, Long>,
    JpaSpecificationExecutor<Sale> {

  // EntityGraph solo carga la relación ManyToOne (client) para evitar producto cartesiano.
  // Las colecciones (items, documents, payments, installments, relatedGuides) se cargan
  // vía @BatchSize(size=30) definido en la entidad: N/30 queries en lugar de N*M*K filas.
  @Override
  @EntityGraph(attributePaths = {
      "client",
      "client.documentType",
      "client.personType",
  })
  @NullMarked
  List<Sale> findAll(Specification<Sale> spec);

  @EntityGraph(attributePaths = {
      "client",
      "client.documentType",
      "client.personType",
      "items",
      "items.product",
      "items.service",
      "items.unitMeasure",
      "documents",
      "documents.documentTypeSunat",
      "payments",
      "payments.paymentMethod",
  })
  Optional<Sale> findByIdAndDeletedAtIsNull(Long id);

  Optional<Sale> findByIdAndCompany_IdAndDeletedAtIsNull(Long id, Long companyId);

  long countBySaleDateBetweenAndDeletedAtIsNull(LocalDateTime start, LocalDateTime end);

  @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
      "WHERE s.saleDate BETWEEN :start AND :end AND s.deletedAt IS NULL")
  BigDecimal sumTotalAmountBySaleDateBetween(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
      "WHERE s.saleDate BETWEEN :start AND :end AND s.currencyCode = :currency " +
      "AND s.deletedAt IS NULL AND s.saleStatus <> 'BORRADOR' " +
      "AND NOT EXISTS (SELECT nc.id FROM CreditDebitNote nc WHERE nc.sale = s " +
      "AND nc.creditDebitNoteType.code = 'C01' AND nc.status = 'ACEPTADO' AND nc.deletedAt IS NULL)")
  BigDecimal sumTotalAmountBySaleDateBetweenAndCurrency(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("currency") String currency);

  @Query("SELECT FUNCTION('DAY', s.saleDate), COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
      "WHERE s.saleDate BETWEEN :start AND :end AND s.deletedAt IS NULL AND s.saleStatus <> 'BORRADOR' " +
      "AND NOT EXISTS (SELECT nc.id FROM CreditDebitNote nc WHERE nc.sale = s " +
      "AND nc.creditDebitNoteType.code = 'C01' AND nc.status = 'ACEPTADO' AND nc.deletedAt IS NULL) " +
      "GROUP BY FUNCTION('DAY', s.saleDate)")
  List<Object[]> sumTotalAmountGroupedByDay(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("SELECT FUNCTION('DAY', s.saleDate), COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
      "WHERE s.saleDate BETWEEN :start AND :end AND s.currencyCode = :currency " +
      "AND s.deletedAt IS NULL AND s.saleStatus <> 'BORRADOR' " +
      "AND NOT EXISTS (SELECT nc.id FROM CreditDebitNote nc WHERE nc.sale = s " +
      "AND nc.creditDebitNoteType.code = 'C01' AND nc.status = 'ACEPTADO' AND nc.deletedAt IS NULL) " +
      "GROUP BY FUNCTION('DAY', s.saleDate)")
  List<Object[]> sumTotalAmountGroupedByDayAndCurrency(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("currency") String currency);

  @EntityGraph(attributePaths = {
      "client",
      "client.documentType",
      "items",
      "items.product",
      "items.service",
      "documents",
      "documents.documentTypeSunat",
  })
  @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :start AND :end " +
      "AND s.deletedAt IS NULL AND s.saleStatus <> 'BORRADOR' " +
      "AND NOT EXISTS (SELECT nc.id FROM CreditDebitNote nc WHERE nc.sale = s " +
      "AND nc.creditDebitNoteType.code = 'C01' AND nc.status = 'ACEPTADO' AND nc.deletedAt IS NULL) " +
      "ORDER BY s.saleDate DESC")
  List<Sale> findForReport(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
