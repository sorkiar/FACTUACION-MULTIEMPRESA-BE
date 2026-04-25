package com.api.multiempresa.dto.external;

import lombok.Data;

@Data
public class GuiaValidRequest {
  private CompanySendRequest empresa;
  private String ticket;
}
