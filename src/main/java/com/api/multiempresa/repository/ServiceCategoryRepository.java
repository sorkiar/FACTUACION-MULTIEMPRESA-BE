package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.ServiceCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceCategoryRepository
    extends JpaRepository<ServiceCategory, Long>,
    JpaSpecificationExecutor<ServiceCategory> {

  Optional<ServiceCategory> findByIdAndDeletedAtIsNull(Long id);

  boolean existsByNameAndCompany_IdAndDeletedAtIsNull(String name, Long companyId);

  boolean existsByNameAndCompany_IdAndDeletedAtIsNullAndIdNot(String name, Long companyId, Long id);
}
