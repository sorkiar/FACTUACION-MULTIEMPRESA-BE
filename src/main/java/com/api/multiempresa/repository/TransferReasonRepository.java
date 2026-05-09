package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.TransferReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransferReasonRepository extends JpaRepository<TransferReason, Long>,
    JpaSpecificationExecutor<TransferReason> {

}
