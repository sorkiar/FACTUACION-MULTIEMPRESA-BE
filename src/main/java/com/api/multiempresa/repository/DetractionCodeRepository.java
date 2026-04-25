package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.DetractionCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DetractionCodeRepository
    extends JpaRepository<DetractionCode, Long>,
    JpaSpecificationExecutor<DetractionCode> {

  Optional<DetractionCode> findByCode(String code);

  List<DetractionCode> findByStatusOrderByCode(Integer status);

  List<DetractionCode> findByCategoryAndStatusOrderByCode(String category, Integer status);
}
