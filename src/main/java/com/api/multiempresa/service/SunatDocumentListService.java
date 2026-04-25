package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.SunatDocumentSummaryResponse;
import java.util.List;

public interface SunatDocumentListService {

  /**
   * Devuelve todos los comprobantes (facturas, boletas, notas, guías) ordenados por
   * fecha de emisión descendente.
   *
   * @param status filtro opcional por estado SUNAT (PENDIENTE, ACEPTADO, RECHAZADO, ERROR).
   *               Si es null, se devuelven todos los estados.
   */
  List<SunatDocumentSummaryResponse> listAll(String status);
}
