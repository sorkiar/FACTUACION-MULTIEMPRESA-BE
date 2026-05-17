package com.api.multiempresa.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CompanyResponse {
  private Long id;
  private String ruc;
  private String businessName;
  private String tradeName;
  private String address;
  private String ubigeo;
  private String phone;
  private String email;
  private String website;
  private String logoUrl;
  private Integer status;

  // ── SUNAT fields (no private key exposed) ─────────────────

  private String sunatEstablishmentCode;
  private Boolean sunatAmazoniaLaw;
  private Boolean sunatProduction;
  private Boolean sunatCertificateLoaded;
  private String sunatSecondaryUser;
  private String sunatSecondaryUserPassword;
  private String sunatGuideId;
  private String sunatGuidePassword;
  private String ubigDepartment;
  private String ubigProvince;
  private String ubigDistrict;
  private String sunatDetractionAccount;

  private LocalDateTime createdAt;
}
