package com.api.multiempresa.controller;

import com.api.multiempresa.dto.request.SunatSendConfigRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.SunatSendConfigResponse;
import com.api.multiempresa.service.SunatSendConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint para gestionar el modo de envío de comprobantes electrónicos a SUNAT.
 *
 * <p>Modo ONLINE: el comprobante se envía al instante en el momento de su creación.
 * <p>Modo OFFLINE: el comprobante queda en estado PENDIENTE y el job programado
 *    lo procesa según el intervalo configurado por tipo de documento.
 *
 * <p>Tipos de documento controlados:
 * <ul>
 *   <li>boleta       – Boleta de Venta (código SUNAT 03)</li>
 *   <li>factura      – Factura (código SUNAT 01)</li>
 *   <li>notaCredito  – Nota de Crédito (código SUNAT 07)</li>
 *   <li>notaDebito   – Nota de Débito (código SUNAT 08)</li>
 *   <li>guiaRemision – Guía de Remisión Remitente (código SUNAT 09)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/sunat-send-config")
@RequiredArgsConstructor
public class SunatSendConfigController {

  private final SunatSendConfigService service;

  /** Obtiene la configuración de envío actual para todos los tipos de comprobante. */
  @GetMapping
  public ResponseEntity<ApiResponse<SunatSendConfigResponse>> getConfig() {
    return ResponseEntity.ok(service.getConfig());
  }

  /**
   * Actualiza la configuración de envío.
   * Solo se actualizan los campos incluidos en el body (campos nulos se ignoran).
   *
   * <p>Ejemplo:
   * <pre>
   * {
   *   "boleta":       { "modo": "OFFLINE", "intervaloMinutos": 15 },
   *   "factura":      { "modo": "ONLINE" },
   *   "notaCredito":  { "modo": "OFFLINE", "intervaloMinutos": 60 },
   *   "notaDebito":   { "modo": "OFFLINE", "intervaloMinutos": 60 },
   *   "guiaRemision": { "modo": "ONLINE" }
   * }
   * </pre>
   */
  @PutMapping
  public ResponseEntity<ApiResponse<SunatSendConfigResponse>> updateConfig(
      @Valid @RequestBody SunatSendConfigRequest request) {
    return ResponseEntity.ok(service.updateConfig(request));
  }
}
