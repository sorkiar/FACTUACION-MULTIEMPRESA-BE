package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class UserResponse {
  private Long id;
  private Long documentTypeId;
  private String documentTypeName;
  private Long profileId;
  private String profileName;
  private String documentNumber;
  private String firstName;
  private String lastName;
  private String username;
  private Integer status;
}
