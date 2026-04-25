package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.ChargeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChargeUnitRepository
    extends JpaRepository<ChargeUnit, Long>,
    JpaSpecificationExecutor<ChargeUnit> {
}
