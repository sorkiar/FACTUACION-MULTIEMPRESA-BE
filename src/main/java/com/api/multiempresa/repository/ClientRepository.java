package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Client;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository
    extends JpaRepository<Client, Long>,
    JpaSpecificationExecutor<Client> {

  @Override
  @EntityGraph(attributePaths = {
      "documentType",
      "personType",
  })
  @NullMarked
  List<Client> findAll(Specification<Client> spec);

  Optional<Client> findByIdAndCompany_IdAndDeletedAtIsNull(Long id, Long companyId);

  boolean existsByDocumentTypeIdAndDocumentNumberAndDeletedAtIsNull(
      Long documentTypeId, String documentNumber);

  boolean existsByDocumentTypeIdAndDocumentNumberAndDeletedAtIsNullAndIdNot(
      Long documentTypeId, String documentNumber, Long id);

  boolean existsByDocumentTypeIdAndDocumentNumberAndCompanyIdAndDeletedAtIsNull(
      Long documentTypeId, String documentNumber, Long companyId);

  boolean existsByDocumentTypeIdAndDocumentNumberAndCompanyIdAndDeletedAtIsNullAndIdNot(
      Long documentTypeId, String documentNumber, Long companyId, Long id);

  long countByCompanyIdAndDeletedAtIsNull(Long companyId);

  long countByCompanyIdAndCreatedAtBetweenAndDeletedAtIsNull(Long companyId, LocalDateTime start, LocalDateTime end);
}
