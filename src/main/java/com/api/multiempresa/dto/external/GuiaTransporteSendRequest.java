package com.api.multiempresa.dto.external;

import lombok.Data;

/**
 * Datos del conductor y vehículo para TRANSPORTE_PRIVADO.
 * Corresponde al bean {@code GuiaTransporte} del proyecto facturador.
 */
@Data
public class GuiaTransporteSendRequest {

  private String gutrConductorTipoDocumento;
  private String gutrConductorNumeroDocumento;
  private String gutrConductorNombres;
  private String gutrConductorApellidos;
  private String gutrConductorNumeroLicencia;
  private String gutrVehiculoPlaca;
  private String gutrVehiculoTipo;
}
