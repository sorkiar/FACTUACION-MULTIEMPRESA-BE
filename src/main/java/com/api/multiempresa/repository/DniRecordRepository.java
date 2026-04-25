package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.DniRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DniRecordRepository extends JpaRepository<DniRecord, String> {
}
