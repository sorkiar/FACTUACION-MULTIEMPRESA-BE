package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemissionGuideDriverRequest {

  /** ID del conductor en el maestro de conductores. */
  @NotNull(message = "driverId es obligatorio")
  private Long driverId;

  /** ID de la placa del maestro de conductores (opcional si se provee vehiclePlate). */
  private Long vehiclePlateId;

  /** Placa personalizada (opcional si se provee vehiclePlateId). */
  private String vehiclePlate;

  @AssertTrue(message = "Se debe indicar vehiclePlateId o vehiclePlate")
  private boolean isPlateProvided() {
    return vehiclePlateId != null || (vehiclePlate != null && !vehiclePlate.isBlank());
  }
}
