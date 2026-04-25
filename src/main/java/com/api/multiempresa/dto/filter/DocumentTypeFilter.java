package com.api.multiempresa.dto.filter;

import lombok.Data;

@Data
public class DocumentTypeFilter {
  private Integer status;
  private Long personTypeId;
}