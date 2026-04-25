package com.api.multiempresa.dto.filter;

import java.time.LocalDate;
import lombok.Data;

@Data
public class CreditDebitNoteFilter {

  private Long id;
  private Long saleId;
  /** "07" para nota de crédito, "08" para nota de débito */
  private String documentTypeCode;
  private String status;
  private LocalDate startDate;
  private LocalDate endDate;
}
