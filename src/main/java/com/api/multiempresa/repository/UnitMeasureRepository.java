package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.UnitMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UnitMeasureRepository
    extends JpaRepository<UnitMeasure, Long>,
    JpaSpecificationExecutor<UnitMeasure> {
}
