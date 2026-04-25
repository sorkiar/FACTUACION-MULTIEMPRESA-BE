package com.api.multiempresa.util;

import com.api.multiempresa.dto.entity.PersonType;
import com.api.multiempresa.dto.request.ClientRequest;
import com.api.multiempresa.exception.BusinessValidationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientValidator {
  public void validateByPersonType(PersonType personType, ClientRequest request) {
    if ("Persona Natural".equalsIgnoreCase(personType.getName())) {
      if (!StringUtils.hasText(request.getFirstName()))
        throw new BusinessValidationException("El nombre es obligatorio para persona natural");

      if (!StringUtils.hasText(request.getLastName()))
        throw new BusinessValidationException("El apellido es obligatorio para persona natural");

      if (Boolean.TRUE.equals(request.getRetentionAgent()))
        throw new BusinessValidationException(
            "Solo una persona jurídica puede ser agente de retención");
    }

    if ("Persona Jurídica".equalsIgnoreCase(personType.getName())) {
      if (!StringUtils.hasText(request.getBusinessName()))
        throw new BusinessValidationException("La razón social es obligatoria");
    }
  }
}
