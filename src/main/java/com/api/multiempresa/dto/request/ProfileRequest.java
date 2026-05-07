package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileRequest {

  @NotBlank(message = "El nombre es obligatorio")
  private String name;

  @NotNull(message = "El estado es obligatorio")
  private Integer status;
}
