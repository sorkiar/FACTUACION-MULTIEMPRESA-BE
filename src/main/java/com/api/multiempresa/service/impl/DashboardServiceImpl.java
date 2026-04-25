package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DashboardResponse;
import com.api.multiempresa.dto.response.MonthlyRevenueResponse;
import com.api.multiempresa.dto.entity.ExchangeRate;
import com.api.multiempresa.repository.ClientRepository;
import com.api.multiempresa.repository.CreditDebitNoteRepository;
import com.api.multiempresa.repository.ExchangeRateRepository;
import com.api.multiempresa.repository.SaleRepository;
import com.api.multiempresa.service.DashboardService;
import com.api.multiempresa.util.TenantContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

  private final ClientRepository clientRepository;
  private final SaleRepository saleRepository;
  private final CreditDebitNoteRepository creditDebitNoteRepository;
  private final ExchangeRateRepository exchangeRateRepository;

  @Override
  public ApiResponse<DashboardResponse> getDashboard() {
    LocalDate today = LocalDate.now();
    LocalDateTime todayStart = today.atStartOfDay();
    LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

    LocalDateTime weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay();
    LocalDateTime weekEnd = today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

    Long companyId = TenantContext.getCurrentCompanyId();

    // Clientes
    long totalClients = clientRepository.countByCompanyIdAndDeletedAtIsNull(companyId);
    long newClientsToday = clientRepository.countByCompanyIdAndCreatedAtBetweenAndDeletedAtIsNull(companyId, todayStart, todayEnd);

    // Ventas
    long totalSalesWeek = saleRepository.countBySaleDateBetweenAndDeletedAtIsNull(weekStart, weekEnd);
    long newSalesToday = saleRepository.countBySaleDateBetweenAndDeletedAtIsNull(todayStart, todayEnd);

    // Ingresos semana por moneda: ventas - notas de crédito + notas de débito (solo ACEPTADO)
    BigDecimal revenueWeekPen = saleRepository.sumTotalAmountBySaleDateBetweenAndCurrency(weekStart, weekEnd, "PEN")
        .subtract(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(weekStart, weekEnd, "CREDITO", "PEN"))
        .add(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(weekStart, weekEnd, "DEBITO", "PEN"));
    BigDecimal revenueWeekUsd = saleRepository.sumTotalAmountBySaleDateBetweenAndCurrency(weekStart, weekEnd, "USD")
        .subtract(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(weekStart, weekEnd, "CREDITO", "USD"))
        .add(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(weekStart, weekEnd, "DEBITO", "USD"));

    // Ingresos hoy por moneda
    BigDecimal revenueTodayPen = saleRepository.sumTotalAmountBySaleDateBetweenAndCurrency(todayStart, todayEnd, "PEN")
        .subtract(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(todayStart, todayEnd, "CREDITO", "PEN"))
        .add(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(todayStart, todayEnd, "DEBITO", "PEN"));
    BigDecimal revenueTodayUsd = saleRepository.sumTotalAmountBySaleDateBetweenAndCurrency(todayStart, todayEnd, "USD")
        .subtract(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(todayStart, todayEnd, "CREDITO", "USD"))
        .add(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(todayStart, todayEnd, "DEBITO", "USD"));

    DashboardResponse data = DashboardResponse.builder()
        .totalClients(totalClients)
        .newClientsToday(newClientsToday)
        .totalSalesWeek(totalSalesWeek)
        .newSalesToday(newSalesToday)
        .revenueWeekPen(revenueWeekPen)
        .revenueWeekUsd(revenueWeekUsd)
        .revenueTodayPen(revenueTodayPen)
        .revenueTodayUsd(revenueTodayUsd)
        .build();

    return new ApiResponse<>("Dashboard obtenido correctamente", data);
  }

  @Override
  public ApiResponse<MonthlyRevenueResponse> getMonthlyRevenue(int year, int month) {
    YearMonth yearMonth = YearMonth.of(year, month);
    LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
    LocalDateTime end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
    int daysInMonth = yearMonth.lengthOfMonth();

    // Tipo de cambio venta por día del mes (para convertir USD → PEN en series)
    Map<LocalDate, BigDecimal> exchangeRates = new HashMap<>();
    for (ExchangeRate er : exchangeRateRepository.findByDateBetweenAndType(
        yearMonth.atDay(1), yearMonth.atEndOfMonth(), "V")) {
      exchangeRates.put(er.getDate(), er.getValue());
    }

    // Mapas por moneda: día → ingreso neto
    Map<Integer, BigDecimal> penByDay = new HashMap<>();
    Map<Integer, BigDecimal> usdByDay = new HashMap<>();
    for (int d = 1; d <= daysInMonth; d++) {
      penByDay.put(d, BigDecimal.ZERO);
      usdByDay.put(d, BigDecimal.ZERO);
    }

    // Ventas PEN y USD por día
    for (Object[] row : saleRepository.sumTotalAmountGroupedByDayAndCurrency(start, end, "PEN")) {
      penByDay.merge(((Number) row[0]).intValue(), (BigDecimal) row[1], BigDecimal::add);
    }
    for (Object[] row : saleRepository.sumTotalAmountGroupedByDayAndCurrency(start, end, "USD")) {
      usdByDay.merge(((Number) row[0]).intValue(), (BigDecimal) row[1], BigDecimal::add);
    }

    // NC (crédito) PEN y USD por día (restan)
    for (Object[] row : creditDebitNoteRepository.sumTotalAmountGroupedByDayAndNoteCategoryAndCurrency(start, end, "CREDITO", "PEN")) {
      penByDay.merge(((Number) row[0]).intValue(), ((BigDecimal) row[1]).negate(), BigDecimal::add);
    }
    for (Object[] row : creditDebitNoteRepository.sumTotalAmountGroupedByDayAndNoteCategoryAndCurrency(start, end, "CREDITO", "USD")) {
      usdByDay.merge(((Number) row[0]).intValue(), ((BigDecimal) row[1]).negate(), BigDecimal::add);
    }

    // ND (débito) PEN y USD por día (suman)
    for (Object[] row : creditDebitNoteRepository.sumTotalAmountGroupedByDayAndNoteCategoryAndCurrency(start, end, "DEBITO", "PEN")) {
      penByDay.merge(((Number) row[0]).intValue(), (BigDecimal) row[1], BigDecimal::add);
    }
    for (Object[] row : creditDebitNoteRepository.sumTotalAmountGroupedByDayAndNoteCategoryAndCurrency(start, end, "DEBITO", "USD")) {
      usdByDay.merge(((Number) row[0]).intValue(), (BigDecimal) row[1], BigDecimal::add);
    }

    // Series en PEN: convertir USD por el TC de la fecha (o el último conocido)
    List<Integer> categories = new ArrayList<>();
    List<BigDecimal> series = new ArrayList<>();
    BigDecimal lastRate = BigDecimal.ONE;
    for (int d = 1; d <= daysInMonth; d++) {
      categories.add(d);
      LocalDate date = yearMonth.atDay(d);
      if (exchangeRates.containsKey(date)) {
        lastRate = exchangeRates.get(date);
      }
      BigDecimal dayTotal = penByDay.get(d)
          .add(usdByDay.get(d).multiply(lastRate))
          .setScale(2, RoundingMode.HALF_UP);
      series.add(dayTotal);
    }

    // Totales del mes por moneda (sin conversión, separados)
    BigDecimal totalPen = saleRepository.sumTotalAmountBySaleDateBetweenAndCurrency(start, end, "PEN")
        .subtract(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(start, end, "CREDITO", "PEN"))
        .add(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(start, end, "DEBITO", "PEN"));
    BigDecimal totalUsd = saleRepository.sumTotalAmountBySaleDateBetweenAndCurrency(start, end, "USD")
        .subtract(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(start, end, "CREDITO", "USD"))
        .add(creditDebitNoteRepository.sumTotalAmountByIssueDateBetweenAndNoteCategoryAndCurrency(start, end, "DEBITO", "USD"));

    return new ApiResponse<>("Ingresos mensuales obtenidos correctamente",
        MonthlyRevenueResponse.builder()
            .year(year)
            .month(month)
            .categories(categories)
            .series(series)
            .totalPen(totalPen)
            .totalUsd(totalUsd)
            .build());
  }
}
