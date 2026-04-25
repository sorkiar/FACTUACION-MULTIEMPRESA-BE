package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientStatusRequest {
  @NotNull(message = "El estado es obligatorio")
  private Integer status;
}
