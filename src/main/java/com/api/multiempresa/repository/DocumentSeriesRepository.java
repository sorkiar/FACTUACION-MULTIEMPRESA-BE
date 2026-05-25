package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.DocumentSeries;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentSeriesRepository
    extends JpaRepository<DocumentSeries, Long>,
    JpaSpecificationExecutor<DocumentSeries> {

  @EntityGraph(attributePaths = {"documentTypeSunat"})
  Optional<DocumentSeries> findByIdAndStatusNot(Long id, Integer status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT ds FROM DocumentSeries ds JOIN FETCH ds.documentTypeSunat WHERE ds.id = :id AND ds.company.id = :companyId")
  Optional<DocumentSeries> findByIdAndCompanyIdForUpdate(@Param("id") Long id, @Param("companyId") Long companyId);

  @EntityGraph(attributePaths = {"documentTypeSunat"})
  Optional<DocumentSeries> findFirstByDocumentTypeSunat_CodeAndStatusNotOrderByIdAsc(
      String code,
      Integer status
  );

  @EntityGraph(attributePaths = {"documentTypeSunat"})
  Optional<DocumentSeries> findFirstByDocumentTypeSunat_CodeAndStatusNotAndCompanyIdOrderByIdAsc(
      String code,
      Integer status,
      Long companyId
  );

  @EntityGraph(attributePaths = {"documentTypeSunat"})
  Optional<DocumentSeries> findFirstByDocumentTypeSunat_CodeAndStatusAndCompany_IdOrderByIdAsc(
      String code, Integer status, Long companyId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT ds FROM DocumentSeries ds JOIN FETCH ds.documentTypeSunat WHERE ds.documentTypeSunat.code = :code AND ds.status = :status AND ds.company.id = :companyId ORDER BY ds.id ASC")
  List<DocumentSeries> findActiveByDocumentTypeCodeAndCompanyForUpdate(@Param("code") String code, @Param("status") Integer status, @Param("companyId") Long companyId);

  @Override
  @EntityGraph(attributePaths = {"documentTypeSunat"})
  Optional<DocumentSeries> findById(Long id);

  @EntityGraph(attributePaths = {"documentTypeSunat"})
  List<DocumentSeries> findByCompany_IdOrderByDocumentTypeSunat_CodeAscSeriesAsc(Long companyId);

  @EntityGraph(attributePaths = {"documentTypeSunat"})
  Optional<DocumentSeries> findByIdAndCompany_Id(Long id, Long companyId);

  long countByCompany_IdAndDocumentTypeSunat_CodeAndStatus(Long companyId, String code, Integer status);

  long countByCompany_IdAndDocumentTypeSunat_CodeAndParentDocumentTypeCodeAndStatus(
      Long companyId, String code, String parentDocumentTypeCode, Integer status);
}
