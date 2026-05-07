package com.api.multiempresa.dto.filter;

import lombok.Data;

@Data
public class ProfileFilter {
  private Long companyId;
  private String name;
  private Integer status;
}
