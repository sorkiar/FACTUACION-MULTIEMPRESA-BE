package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class PaymentMethodResponse {
  private Integer id;
  private String code;
  private String name;
  private Boolean requiresProof;
  private Integer status;
}
