package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class UbigeoResponse {
  private String ubigeo;
  private String department;
  private String province;
  private String district;
  private Integer status;
}
