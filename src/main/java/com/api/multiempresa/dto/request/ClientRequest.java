package com.api.multiempresa.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ClientRequest {
  @NotNull(message = "El tipo de persona es obligatorio")
  private Long personTypeId;

  @NotNull(message = "El tipo de documento es obligatorio")
  private Long documentTypeId;

  @NotBlank(message = "El número de documento es obligatorio")
  private String documentNumber;

  // Persona Natural
  private String firstName;
  private String lastName;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate birthDate;

  // Persona Jurídica
  private String businessName;
  private String contactPersonName;

  private String countryCode1;
  private String phone1;
  private String countryCode2;
  private String phone2;
  private String email1;
  private String email2;

  private Boolean retentionAgent;

  @Valid
  private List<ClientAddressRequest> addresses;
}
