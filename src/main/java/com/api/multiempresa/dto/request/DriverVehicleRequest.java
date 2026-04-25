package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverVehicleRequest {

  @NotBlank(message = "plate es obligatorio")
  private String plate;
}
