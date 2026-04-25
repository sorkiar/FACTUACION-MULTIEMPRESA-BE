package com.api.multiempresa.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ClientResponse {
  private Long id;
  private String personTypeId;
  private String personType;
  private String documentTypeId;
  private String documentType;
  private String documentNumber;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private String businessName;
  private String contactPersonName;
  private String countryCode1;
  private String phone1;
  private String email1;
  private String countryCode2;
  private String phone2;
  private String email2;
  private Boolean retentionAgent;
  private Integer status;
  private List<ClientAddressResponse> addresses;
}
