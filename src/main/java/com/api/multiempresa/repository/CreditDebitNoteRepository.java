package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.CreditDebitNote;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditDebitNoteRepository
    extends JpaRepository<CreditDebitNote, Long>,
    JpaSpecificationExecutor<CreditDebitNote> {

  @Override
  @EntityGraph(attributePaths = {
      "sale",
      "sale.client",
      "sale.client.documentType",
      "sale.client.personType",
      "originalDocument",
      "originalDocument.documentTypeSunat",
      "documentTypeSunat",
      "documentSeries",
      "creditDebitNoteType",
      "items",
  })
  @NullMarked
  List<CreditDebitNote> findAll(Specification<CreditDebitNote> spec);

  @EntityGraph(attributePaths = {
      "sale",
      "sale.client",
      "sale.client.documentType",
      "sale.client.personType",
      "originalDocument",
      "originalDocument.documentTypeSunat",
      "documentTypeSunat",
      "documentSeries",
      "creditDebitNoteType",
      "items",
  })
  Optional<CreditDebitNote> findByIdAndDeletedAtIsNull(Long id);

  @EntityGraph(attributePaths = {
      "sale",
      "sale.client",
      "sale.client.documentType",
      "sale.client.personType",
      "originalDocument",
      "originalDocument.documentTypeSunat",
      "documentTypeSunat",
      "documentSeries",
      "creditDebitNoteType",
      "items",
  })
  Optional<CreditDebitNote> findByIdAndCompany_IdAndDeletedAtIsNull(Long id, Long companyId);

  @EntityGraph(attributePaths = {
      "sale",
      "sale.client",
      "sale.client.documentType",
      "sale.client.personType",
      "originalDocument",
      "originalDocument.documentTypeSunat",
      "documentTypeSunat",
      "documentSeries",
      "creditDebitNoteType",
      "items",
  })
  List<CreditDebitNote> findByStatusAndDocumentTypeSunat_CodeAndDeletedAtIsNull(
      String status, String code);

  @EntityGraph(attributePaths = {"documentTypeSunat", "creditDebitNoteType", "originalDocument"})
  List<CreditDebitNote> findByDeletedAtIsNullOrderByIssueDateDesc();

  @EntityGraph(attributePaths = {"documentTypeSunat", "creditDebitNoteType", "originalDocument"})
  List<CreditDebitNote> findByStatusAndDeletedAtIsNullOrderByIssueDateDesc(String status);

  @EntityGraph(attributePaths = {"documentTypeSunat", "creditDebitNoteType", "originalDocument"})
  List<CreditDebitNote> findByCompany_IdAndDeletedAtIsNullOrderByIssueDateDesc(Long companyId);

  @EntityGraph(attributePaths = {"documentTypeSunat", "creditDebitNoteType", "originalDocument"})
  List<CreditDebitNote> findByCompany_IdAndStatusAndDeletedAtIsNullOrderByIssueDateDesc(Long companyId, String status);

  boolean existsBySale_IdAndCreditDebitNoteType_CodeAndStatusInAndDeletedAtIsNull(
      Long saleId, String code, Collection<String> statuses);

  @Query("SELECT COALESCE(SUM(n.totalAmount), 0) FROM CreditDebitNote n " +
      "WHERE n.issueDate BETWEEN :start AND :end " +
      "AND n.creditDebitNoteType.noteCategory = :noteCategory " +
      "AND n.status = 'ACEPTADO' " +
      "AND n.deletedAt IS NULL")
  BigDecimal sumTotalAmountByIssueDateBetweenAndNoteCategory(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("noteCategory") String noteCategory);

  @Query("SELECT COALESCE(SUM(n.totalAmount), 0) FROM CreditDebitNote n " +
      "WHERE n.issueDate BETWEEN :start AND :end " +
      "AND n.creditDebitNoteType.noteCategory = :noteCategory " +
      "AND n.creditDebitNoteType.code <> 'C01' " +
      "AND n.currencyCode = :currency " +
      "AND n.status = 'ACEPTADO' " +
      "AND n.deletedAt IS NULL")
  BigDecimal sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("noteCategory") String noteCategory,
      @Param("currency") String currency);

  @Query("SELECT FUNCTION('DAY', n.issueDate), COALESCE(SUM(n.totalAmount), 0) FROM CreditDebitNote n " +
      "WHERE n.issueDate BETWEEN :start AND :end " +
      "AND n.creditDebitNoteType.noteCategory = :noteCategory " +
      "AND n.creditDebitNoteType.code <> 'C01' " +
      "AND n.status = 'ACEPTADO' AND n.deletedAt IS NULL " +
      "GROUP BY FUNCTION('DAY', n.issueDate)")
  List<Object[]> sumTotalAmountGroupedByDayAndNoteCategory(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("noteCategory") String noteCategory);

  @Query("SELECT FUNCTION('DAY', n.issueDate), COALESCE(SUM(n.totalAmount), 0) FROM CreditDebitNote n " +
      "WHERE n.issueDate BETWEEN :start AND :end " +
      "AND n.creditDebitNoteType.noteCategory = :noteCategory " +
      "AND n.creditDebitNoteType.code <> 'C01' " +
      "AND n.currencyCode = :currency " +
      "AND n.status = 'ACEPTADO' AND n.deletedAt IS NULL " +
      "GROUP BY FUNCTION('DAY', n.issueDate)")
  List<Object[]> sumTotalAmountGroupedByDayAndNoteCategoryAndCurrency(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("noteCategory") String noteCategory,
      @Param("currency") String currency);
}
