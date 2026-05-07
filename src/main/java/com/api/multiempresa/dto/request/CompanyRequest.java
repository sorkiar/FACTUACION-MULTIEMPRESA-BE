package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRequest {

  @NotBlank
  @Pattern(regexp = "\\d{11}", message = "RUC must be 11 digits")
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

  // ── SUNAT fields ──────────────────────────────────────────

  @Size(max = 10)
  private String sunatEstablishmentCode;

  private Boolean sunatAmazoniaLaw = false;

  private Boolean sunatProduction = false;

  @Size(max = 50)
  private String sunatSecondaryUser;

  @Size(max = 100)
  private String sunatSecondaryUserPassword;

  @Size(max = 50)
  private String sunatGuideId;

  @Size(max = 100)
  private String sunatGuidePassword;

  @Size(max = 100)
  private String ubigDepartment;

  @Size(max = 100)
  private String ubigProvince;

  @Size(max = 100)
  private String ubigDistrict;

  @Size(max = 20)
  private String sunatDetractionAccount;
}
