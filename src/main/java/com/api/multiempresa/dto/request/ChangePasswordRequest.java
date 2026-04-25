package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
  @NotBlank(message = "La nueva contraseña es obligatoria")
  private String newPassword;
}
