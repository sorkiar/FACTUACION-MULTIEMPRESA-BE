package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.SalesReportResponse;
import java.time.LocalDate;

public interface ReportService {
  SalesReportResponse salesReport(LocalDate startDate, LocalDate endDate, String clientIds, String productIds);
}
