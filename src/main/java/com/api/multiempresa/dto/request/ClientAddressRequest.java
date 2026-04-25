package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientAddressRequest {

  @NotBlank(message = "address es obligatorio")
  private String address;

  private String ubigeo;

  private String description;
}
