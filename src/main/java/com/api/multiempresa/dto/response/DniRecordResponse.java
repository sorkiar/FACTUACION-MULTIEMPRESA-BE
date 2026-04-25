package com.api.multiempresa.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DniRecordResponse {

  private String docNumber;
  private String fullName;
  private String firstName;
  private String lastNamePaternal;
  private String lastNameMaternal;
  private String verificationCode;
  private String address;
  private String fullAddress;
  private String ubigeoReniec;
  private String ubigeoSunat;
  private String ubigeoDept;
  private String ubigeoProv;
  private String ubigeoDist;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
