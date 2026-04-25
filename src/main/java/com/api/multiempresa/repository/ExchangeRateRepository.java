package com.api.multiempresa.repository;

import com.api.multiempresa.dto.entity.ExchangeRate;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

  boolean existsByDateAndType(LocalDate date, String type);

  boolean existsByDateAndTypeAndCompanyId(LocalDate date, String type, Long companyId);

  java.util.Optional<ExchangeRate> findByDateAndType(LocalDate date, String type);

  java.util.Optional<ExchangeRate> findByDateAndTypeAndCompanyId(LocalDate date, String type, Long companyId);

  List<ExchangeRate> findByDate(LocalDate date);

  List<ExchangeRate> findByDateAndCompanyId(LocalDate date, Long companyId);

  List<ExchangeRate> findByDateBetweenAndType(LocalDate start, LocalDate end, String type);

  List<ExchangeRate> findByDateBetweenAndTypeAndCompanyId(LocalDate start, LocalDate end, String type, Long companyId);
}
