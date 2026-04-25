package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.Ubigeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UbigeoRepository
    extends JpaRepository<Ubigeo, String>,
    JpaSpecificationExecutor<Ubigeo> {
}
