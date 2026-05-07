package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.DocumentType;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentTypeRepository
    extends JpaRepository<DocumentType, Long>,
    JpaSpecificationExecutor<DocumentType> {

  Optional<DocumentType> findByCode(String code);

  @Override
  @EntityGraph(attributePaths = {
      "personType",
  })
  @NullMarked
  List<DocumentType> findAll(Specification<DocumentType> spec);
}
