package com.api.multiempresa.controller;

import com.api.multiempresa.dto.request.BulkImportExchangeRateRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ExchangeRateResponse;
import com.api.multiempresa.service.ExchangeRateService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

  private final ExchangeRateService exchangeRateService;

  @GetMapping
  public ResponseEntity<ApiResponse<ExchangeRateResponse>> findByDate(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    if (date == null) {
      date = LocalDate.now();
    }
    return ResponseEntity.ok(exchangeRateService.findByDate(date));
  }

  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @PostMapping("/bulk")
  public ResponseEntity<ApiResponse<List<ExchangeRateResponse>>> bulkImport(
      @RequestBody @Valid BulkImportExchangeRateRequest request
  ) {
    return ResponseEntity.ok(
        exchangeRateService.bulkImport(request.getFrom(), request.getTo()));
  }
}
