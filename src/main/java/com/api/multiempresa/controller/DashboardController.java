package com.api.multiempresa.controller;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DashboardResponse;
import com.api.multiempresa.dto.response.MonthlyRevenueResponse;
import com.api.multiempresa.service.DashboardService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService service;

  @GetMapping
  public ApiResponse<DashboardResponse> getDashboard() {
    return service.getDashboard();
  }

  @GetMapping("/monthly-revenue")
  public ApiResponse<MonthlyRevenueResponse> getMonthlyRevenue(
      @RequestParam(defaultValue = "0") int year,
      @RequestParam(defaultValue = "0") int month
  ) {
    LocalDate now = LocalDate.now();
    int y = year > 0 ? year : now.getYear();
    int m = month > 0 ? month : now.getMonthValue();
    return service.getMonthlyRevenue(y, m);
  }
}
