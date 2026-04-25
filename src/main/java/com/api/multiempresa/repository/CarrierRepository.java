package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CarrierRepository
    extends JpaRepository<Carrier, Long>,
    JpaSpecificationExecutor<Carrier> {
}
