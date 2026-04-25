package com.api.multiempresa.service;

import com.api.multiempresa.dto.request.SunatSendConfigRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.SunatSendConfigResponse;

public interface SunatSendConfigService {

  ApiResponse<SunatSendConfigResponse> getConfig();

  ApiResponse<SunatSendConfigResponse> updateConfig(SunatSendConfigRequest request);

  /**
   * Devuelve true si el tipo de comprobante está en modo ONLINE.
   *
   * @param docTypeKey clave del tipo: "boleta", "factura", "nota_credito",
   *                   "nota_debito" o "guia_remision"
   */
  boolean isOnlineMode(String docTypeKey);

  /**
   * Devuelve el intervalo configurado en minutos para el modo OFFLINE.
   *
   * @param docTypeKey clave del tipo de comprobante
   */
  int getIntervalMinutes(String docTypeKey);
}
