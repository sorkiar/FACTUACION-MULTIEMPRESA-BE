package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRequest {

  @NotBlank
  @Pattern(regexp = "\\d{11}", message = "RUC debe tener 11 dígitos")
  private String ruc;

  @NotBlank
  @Size(max = 200)
  private String businessName;

  @Size(max = 200)
  private String tradeName;

  @Size(max = 500)
  private String address;

  @Size(max = 10)
  private String ubigeo;

  @Size(max = 30)
  private String phone;

  @Size(max = 150)
  private String email;

  @Size(max = 200)
  private String website;

  @Size(max = 500)
  private String logoUrl;

  private Integer status = 1;
}
