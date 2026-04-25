package com.api.multiempresa.dto.external;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DocumentSendRequest {
  private String compSerie;
  private Integer compNumero;
  private BigDecimal compPorcentajeIgv;
  private String compCondicionPago;
  private String compMedioPago;
  private String compFechaEmision;
  private String compHoraEmision;
  private String compObservaciones;
  private String moneda;
  private String tipoDocumento;
  private String tipoOperacion;
  private ClientSendRequest cliente;
  private List<ItemSendRequest> lsItemComprobante;
  private List<ItemCuotaSendRequest> lsItemCuota;
  private NotaSendRequest nota;
  private GuiaSendRequest guia;
  private Boolean compDetraccion;
  private String compCuentaDetraccion;
  private List<DetraccionSendRequest> lsDetraccion;
}
