package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.PersonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonTypeRepository
    extends JpaRepository<PersonType, Long>,
    JpaSpecificationExecutor<PersonType> {
}
