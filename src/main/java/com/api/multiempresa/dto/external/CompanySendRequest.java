package com.api.multiempresa.dto.external;

import lombok.Data;

@Data
public class CompanySendRequest {
  private String emprRuc;
  private String emprRazonSocial;
  private String emprDireccionFiscal;
  private String emprCodigoEstablecimientoSunat;
  private Boolean emprLeyAmazonia;
  private Boolean emprProduccion;
  private String emprCertificadoLLavePublica;
  private String emprCertificadoLLavePrivada;
  private String emprUsuarioSecundario;
  private String emprClaveUsuarioSecundario;
  private UbigeoSendRequest ubigeo;

  // Credenciales adicionales para guías de remisión
  private String emprGuiaId;
  private String emprGuiaClave;
}
