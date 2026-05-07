package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
  @NotBlank(message = "ruc es requerido")
  private String ruc;
  @NotBlank(message = "username es requerido")
  private String username;
  @NotBlank(message = "password es requerido")
  private String password;
}
