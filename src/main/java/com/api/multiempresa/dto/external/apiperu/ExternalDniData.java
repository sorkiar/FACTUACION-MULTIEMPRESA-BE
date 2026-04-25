package com.api.multiempresa.dto.external.apiperu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalDniData {

  @JsonProperty("numero")
  private String docNumber;

  @JsonProperty("nombre_completo")
  private String fullName;

  @JsonProperty("nombres")
  private String firstName;

  @JsonProperty("apellido_paterno")
  private String lastNamePaternal;

  @JsonProperty("apellido_materno")
  private String lastNameMaternal;

  @JsonProperty("codigo_verificacion")
  private String verificationCode;

  @JsonProperty("direccion")
  private String address;

  @JsonProperty("direccion_completa")
  private String fullAddress;

  @JsonProperty("ubigeo_reniec")
  private String ubigeoReniec;

  @JsonProperty("ubigeo_sunat")
  private String ubigeoSunat;

  @JsonProperty("ubigeo")
  private String[] ubigeo;
}
