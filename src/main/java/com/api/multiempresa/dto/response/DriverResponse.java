package com.api.multiempresa.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class DriverResponse {

  private Long id;
  private String docType;
  private String docNumber;
  private String firstName;
  private String lastName;
  private String licenseNumber;
  private Integer status;

  /** Populated only in findById; null in list responses. */
  private List<DriverVehicleResponse> vehicles;
}
