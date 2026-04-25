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
  private LocalDateTime createdAt;
}
