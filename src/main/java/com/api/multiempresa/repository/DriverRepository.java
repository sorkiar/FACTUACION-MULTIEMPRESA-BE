package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DriverRepository
    extends JpaRepository<Driver, Long>,
    JpaSpecificationExecutor<Driver> {
}
