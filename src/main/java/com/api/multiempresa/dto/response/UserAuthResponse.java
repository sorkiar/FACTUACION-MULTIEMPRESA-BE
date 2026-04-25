package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class UserAuthResponse {
  private Long id;
  private String documentType;
  private String documentNumber;
  private String profile;
  private String firstName;
  private String lastName;
  private String username;
  private String token;
  private Integer status;
  private Long companyId;
  private String companyName;
}
