package com.api.multiempresa.controller;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.SalesReportResponse;
import com.api.multiempresa.service.ReportService;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

  private final ReportService reportService;

  @GetMapping("/sales")
  public ResponseEntity<ApiResponse<SalesReportResponse>> salesReport(
      @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false) String clientIds,
      @RequestParam(required = false) String productIds
  ) {
    SalesReportResponse data = reportService.salesReport(startDate, endDate, clientIds, productIds);
    return ResponseEntity.ok(ApiResponse.<SalesReportResponse>builder()
        .message("Reporte de ventas generado correctamente")
        .data(data)
        .build());
  }
}
