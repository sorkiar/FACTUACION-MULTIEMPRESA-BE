package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class RemissionGuideDriverResponse {

  private Long id;
  private DriverResponse driver;
  private DriverVehicleResponse driverVehicle;
  private String vehiclePlate;
}
