package com.api.multiempresa.dto.external;

import lombok.Data;

@Data
public class SunatSendRequest {
  private CompanySendRequest empresa;
  private DocumentSendRequest comprobante;
}
