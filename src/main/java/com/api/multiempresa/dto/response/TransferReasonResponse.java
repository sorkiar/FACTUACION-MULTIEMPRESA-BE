package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class TransferReasonResponse {
  private Long id;
  private String code;
  private String name;
  private Integer status;
}
