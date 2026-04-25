package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class RemissionGuideResponse {

  private Long id;
  private Long documentSeriesId;
  private String series;
  private String sequence;
  private LocalDateTime issueDate;
  private LocalDate transferDate;

  private String transferReason;
  private String transferReasonDescription;
  private String transportMode;

  private BigDecimal grossWeight;
  private String weightUnit;
  private Integer packageCount;

  // Punto de partida
  private String originAddress;
  private String originUbigeo;
  private String originLocalCode;

  // Punto de llegada
  private String destinationAddress;
  private String destinationUbigeo;
  private String destinationLocalCode;

  private Boolean minorVehicleTransfer;

  // Destinatario (cliente)
  private ClientResponse client;
  /** Dirección del cliente usada en el comprobante. */
  private String clientAddress;
  /** Dirección registrada seleccionada (referencial, nullable). */
  private ClientAddressResponse selectedClientAddress;

  // Transportista (TRANSPORTE_PUBLICO)
  private CarrierResponse carrier;

  private String observations;

  // Estado SUNAT
  private String status;
  private Integer sunatResponseCode;
  private String sunatMessage;
  private String hashCode;
  private String xmlUrl;
  private String cdrUrl;
  private String pdfUrl;

  private List<RemissionGuideItemResponse> items;
  private List<RemissionGuideDriverResponse> drivers;
}
