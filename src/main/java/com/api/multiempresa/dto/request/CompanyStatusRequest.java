package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyStatusRequest {
  @NotNull(message = "El estado es obligatorio")
  private Integer status; // 1=Activo, 0=Inactivo
}
