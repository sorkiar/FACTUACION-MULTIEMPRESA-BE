package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryRepository
    extends JpaRepository<Category, Long>,
    JpaSpecificationExecutor<Category> {

  Optional<Category> findByIdAndDeletedAtIsNull(Long id);

  boolean existsByNameAndCompany_IdAndDeletedAtIsNull(String name, Long companyId);

  boolean existsByNameAndCompany_IdAndDeletedAtIsNullAndIdNot(String name, Long companyId, Long id);
}
