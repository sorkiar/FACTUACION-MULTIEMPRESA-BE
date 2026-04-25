package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ExchangeRateResponse {
  private LocalDate date;
  private BigDecimal purchase;
  private BigDecimal sale;
}
