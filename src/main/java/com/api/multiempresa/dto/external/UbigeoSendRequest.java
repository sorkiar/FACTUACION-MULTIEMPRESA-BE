package com.api.multiempresa.dto.external;

import lombok.Data;

@Data
public class UbigeoSendRequest {
  private Integer ubigId;
  private String ubigUbigeo;
  private String ubigDpto;
  private String ubigProv;
  private String ubigDist;
  private String ubigDistrito;
  private String ubigProvincia;
  private String ubigDepartamento;
  private Boolean ubigEstado;
}
