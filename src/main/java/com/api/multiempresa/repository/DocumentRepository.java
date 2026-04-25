package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Document;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentRepository
    extends JpaRepository<Document, Long>,
    JpaSpecificationExecutor<Document> {

  @Override
  @EntityGraph(attributePaths = {
      "sale",
      "sale.client",
      "documentTypeSunat",
      "documentSeries",
      "documentSeries.documentTypeSunat",
  })
  @NullMarked
  List<Document> findAll(Specification<Document> spec);

  @EntityGraph(attributePaths = {
      "sale",
      "sale.client",
      "sale.items",
      "documentTypeSunat",
      "documentSeries",
      "documentSeries.documentTypeSunat",
  })
  List<Document> findByStatusAndDocumentTypeSunat_CodeAndDeletedAtIsNull(String status, String code);

  @EntityGraph(attributePaths = {
      "sale",
      "sale.client",
      "sale.items",
      "documentTypeSunat",
      "documentSeries",
      "documentSeries.documentTypeSunat",
  })
  Optional<Document> findBySaleId(Long saleId);

  @EntityGraph(attributePaths = {"documentTypeSunat"})
  List<Document> findByDeletedAtIsNullOrderByIssueDateDesc();

  @EntityGraph(attributePaths = {"documentTypeSunat"})
  List<Document> findByStatusAndDeletedAtIsNullOrderByIssueDateDesc(String status);
}
