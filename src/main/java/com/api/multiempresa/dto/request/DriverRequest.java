package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverRequest {

  /** Tipo de documento. Default DNI. */
  private String docType = "DNI";

  @NotBlank(message = "docNumber es obligatorio")
  private String docNumber;

  @NotBlank(message = "firstName es obligatorio")
  private String firstName;

  @NotBlank(message = "lastName es obligatorio")
  private String lastName;

  @NotBlank(message = "licenseNumber es obligatorio")
  private String licenseNumber;
}
