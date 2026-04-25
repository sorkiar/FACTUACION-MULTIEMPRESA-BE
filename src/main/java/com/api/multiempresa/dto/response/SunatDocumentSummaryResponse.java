package com.api.multiempresa.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Vista unificada de todos los comprobantes electrónicos enviados o pendientes de envío a SUNAT:
 * facturas, boletas, notas de crédito/débito y guías de remisión.
 */
@Data
@Builder
public class SunatDocumentSummaryResponse {

  private Long id;

  /** DOCUMENTO | NOTA | GUIA */
  private String category;

  /** Código de tipo SUNAT: 01, 03, 07, 08, 09 */
  private String documentTypeCode;

  /** Nombre del tipo: FACTURA, BOLETA, NOTA_CREDITO, NOTA_DEBITO, GUIA_REMISION_REMITENTE */
  private String documentTypeName;

  private String series;
  private String sequence;

  /** serie-correlativo (ej. F001-00000001) */
  private String voucherNumber;

  private LocalDateTime issueDate;

  /** PENDIENTE | ACEPTADO | RECHAZADO | ERROR */
  private String status;

  private Integer sunatResponseCode;
  private String sunatMessage;
  private String hashCode;

  private boolean hasXml;
  private boolean hasCdr;
  private boolean hasPdf;

  /** Links de descarga en Drive */
  private String pdfUrl;
  private String xmlUrl;
  private String cdrUrl;

  // ---- Solo notas de crédito / débito ----

  /** Código catálogo 09/10 SUNAT: C01, D05, etc. */
  private String noteCode;

  /** CREDITO | DEBITO */
  private String noteCategory;

  /** Descripción del tipo de nota */
  private String noteTypeName;

  /** Comprobante afectado, ej. F001-00000123 */
  private String originalVoucher;

  // ---- Solo guías de remisión ----

  /** Catálogo 20: VENTA, COMPRA, TRASLADO_EMPRESA, etc. */
  private String transferReason;

  private LocalDate transferDate;

  /** TRANSPORTE_PUBLICO | TRANSPORTE_PRIVADO */
  private String transportMode;
}
