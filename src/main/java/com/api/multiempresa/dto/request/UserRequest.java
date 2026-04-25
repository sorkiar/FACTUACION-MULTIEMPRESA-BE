package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {
  @NotNull(message = "El tipo de documento es obligatorio")
  private Long documentTypeId;

  @NotNull(message = "El perfil es obligatorio")
  private Long profileId;

  @NotBlank(message = "El número de documento es obligatorio")
  private String documentNumber;

  private String firstName;
  private String lastName;

  @NotBlank(message = "El usuario es obligatorio")
  private String username;

  @NotBlank(message = "La contraseña es obligatoria")
  private String password;
}
