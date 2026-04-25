package com.api.multiempresa.dto.external;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * Campos del objeto {@code guia} dentro del comprobante enviado al facturador.
 * Corresponde al bean {@code Guia} del proyecto facturador.
 */
@Data
public class GuiaSendRequest {

  private BigDecimal guiaPesoBruto;
  private Integer guiaNumeroBultos;
  private String guiaUnidadMedidaPeso;
  private String guiaFechaTraslado;

  // Punto de partida
  private String guiaPuntoPartidaDireccion;
  private String guiaUbigeoPartida;
  private String guiaCodigoLocalPartida;

  // Punto de llegada
  private String guiaPuntoLlegadaDireccion;
  private String guiaUbigeoLlegada;
  private String guiaCodigoLocalLlegada;

  /**
   * Nombre del enum {@code Catalog20MotivoTraslado} del facturador.
   * Ej: VENTA, COMPRA, TRASLADO_EMPRESA, OTROS, EXPORTACION, TRASLADO_ITINERANTE.
   */
  private String motivoTraslado;

  private String guiaMotivoTrasladoDescripcion;

  /**
   * Nombre del enum {@code Catalog18ModalidadTransporte} del facturador.
   * Valores: TRANSPORTE_PUBLICO | TRANSPORTE_PRIVADO
   */
  private String tipoTransporte;

  private Boolean guiaTrasladoVehiculoMenores;

  /** Transportista RUC (solo para TRANSPORTE_PUBLICO) */
  private ClientSendRequest transportista;

  /** Conductores / vehículos (solo para TRANSPORTE_PRIVADO) */
  private List<GuiaTransporteSendRequest> lsGuiaTransporte;
}
