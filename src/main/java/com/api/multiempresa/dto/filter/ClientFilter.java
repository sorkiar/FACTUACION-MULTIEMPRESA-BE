package com.api.multiempresa.dto.filter;

import lombok.Data;

@Data
public class ClientFilter {
  private Long id;
  private Integer status;
  private Long documentTypeId;
  private String documentNumber;
}
