package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.entity.ExchangeRate;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ExchangeRateResponse;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.job.ExchangeRateJobService;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.ExchangeRateRepository;
import com.api.multiempresa.service.ExchangeRateService;
import com.api.multiempresa.util.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

  private final ExchangeRateRepository exchangeRateRepository;
  private final ExchangeRateJobService exchangeRateJobService;
  private final CompanyRepository companyRepository;

  @Override
  public ApiResponse<ExchangeRateResponse> findByDate(LocalDate date) {
    Long companyId = TenantContext.getCompanyId();
    List<ExchangeRate> rates = (companyId != null)
        ? exchangeRateRepository.findByDateAndCompanyId(date, companyId)
        : exchangeRateRepository.findByDate(date);

    if (rates.isEmpty()) {
      throw new ResourceNotFoundException(
          "No se encontró tipo de cambio para la fecha: " + date);
    }

    ExchangeRateResponse response = new ExchangeRateResponse();
    response.setDate(date);

    for (ExchangeRate rate : rates) {
      if ("C".equals(rate.getType())) {
        response.setPurchase(rate.getValue());
      } else if ("V".equals(rate.getType())) {
        response.setSale(rate.getValue());
      }
    }

    return new ApiResponse<>("Tipo de cambio obtenido correctamente", response);
  }

  @Override
  public ApiResponse<List<ExchangeRateResponse>> bulkImport(LocalDate from, LocalDate to) {
    if (from.getYear() != to.getYear() || from.getMonth() != to.getMonth()) {
      throw new IllegalArgumentException("Las fechas deben estar en el mismo mes y año");
    }
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("La fecha inicial no puede ser mayor a la final");
    }

    List<Company> companies = companyRepository.findByDeletedAtIsNull();
    exchangeRateJobService.fetchAndSaveRange(from, to, companies);

    Map<LocalDate, BigDecimal> purchaseMap = exchangeRateRepository
        .findByDateBetweenAndType(from, to, "C").stream()
        .collect(Collectors.toMap(ExchangeRate::getDate, ExchangeRate::getValue, (a, b) -> a));
    Map<LocalDate, BigDecimal> saleMap = exchangeRateRepository
        .findByDateBetweenAndType(from, to, "V").stream()
        .collect(Collectors.toMap(ExchangeRate::getDate, ExchangeRate::getValue, (a, b) -> a));

    Map<LocalDate, ExchangeRateResponse> byDate = new TreeMap<>();
    purchaseMap.forEach((date, value) -> {
      ExchangeRateResponse r = byDate.computeIfAbsent(date, d -> new ExchangeRateResponse());
      r.setDate(date);
      r.setPurchase(value);
    });
    saleMap.forEach((date, value) -> {
      ExchangeRateResponse r = byDate.computeIfAbsent(date, d -> new ExchangeRateResponse());
      r.setDate(date);
      r.setSale(value);
    });

    return new ApiResponse<>("Tipos de cambio importados correctamente", List.copyOf(byDate.values()));
  }
}
