package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceCategoryRepository
    extends JpaRepository<ServiceCategory, Long>,
    JpaSpecificationExecutor<ServiceCategory> {
}
