package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UbigeoRequest {

  @NotBlank(message = "El código de ubigeo es obligatorio")
  @Size(max = 8, message = "El código de ubigeo no puede superar 8 caracteres")
  private String ubigeo;

  @Size(max = 50)
  private String department;

  @Size(max = 50)
  private String province;

  @Size(max = 50)
  private String distrit;
}
