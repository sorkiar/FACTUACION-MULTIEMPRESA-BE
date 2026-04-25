package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ExchangeRateResponse;
import java.time.LocalDate;
import java.util.List;

public interface ExchangeRateService {
  ApiResponse<ExchangeRateResponse> findByDate(LocalDate date);
  ApiResponse<List<ExchangeRateResponse>> bulkImport(LocalDate from, LocalDate to);
}
