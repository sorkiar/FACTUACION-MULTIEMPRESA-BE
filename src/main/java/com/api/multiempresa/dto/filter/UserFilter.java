package com.api.multiempresa.dto.filter;

import lombok.Data;

@Data
public class UserFilter {
  private Long id;
  private String username;
  private Long documentTypeId;
  private String documentNumber;
  private Integer status;
}
