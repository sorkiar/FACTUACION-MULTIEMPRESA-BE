package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.ExchangeRate;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

  boolean existsByDateAndType(LocalDate date, String type);

  Optional<ExchangeRate> findByDateAndType(LocalDate date, String type);

  List<ExchangeRate> findByDate(LocalDate date);

  List<ExchangeRate> findByDateBetweenAndType(LocalDate start, LocalDate end, String type);
}
