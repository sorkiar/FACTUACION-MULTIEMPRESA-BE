package com.api.multiempresa.dto.filter;

import java.time.LocalDate;
import lombok.Data;

@Data
public class DocumentFilter {
  private Long id;
  private Long saleId;
  private LocalDate issueDate;
}
