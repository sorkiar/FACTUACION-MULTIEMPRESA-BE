package com.api.multiempresa.validation;

import com.api.multiempresa.dto.request.CompanyRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CompanyValidator implements ConstraintValidator<ValidCompany, CompanyRequest> {

  @Override
  public boolean isValid(CompanyRequest req, ConstraintValidatorContext ctx) {
    ctx.disableDefaultConstraintViolation();
    boolean valid = true;

    if (Boolean.TRUE.equals(req.getSunatProduction())
        && req.getSunatSecondaryUser() != null
        && req.getSunatSecondaryUser().toUpperCase().contains("MODDATOS")) {
      ctx.buildConstraintViolationWithTemplate(
              "En modo producción el usuario secundario SUNAT no puede contener 'MODDATOS'")
          .addPropertyNode("sunatSecondaryUser")
          .addConstraintViolation();
      valid = false;
    }

    if (req.getUbigeo() != null && !req.getUbigeo().isBlank()) {
      if (isBlank(req.getUbigDepartment())) {
        ctx.buildConstraintViolationWithTemplate("El departamento es requerido cuando se especifica ubigeo")
            .addPropertyNode("ubigDepartment").addConstraintViolation();
        valid = false;
      }
      if (isBlank(req.getUbigProvince())) {
        ctx.buildConstraintViolationWithTemplate("La provincia es requerida cuando se especifica ubigeo")
            .addPropertyNode("ubigProvince").addConstraintViolation();
        valid = false;
      }
      if (isBlank(req.getUbigDistrict())) {
        ctx.buildConstraintViolationWithTemplate("El distrito es requerido cuando se especifica ubigeo")
            .addPropertyNode("ubigDistrict").addConstraintViolation();
        valid = false;
      }
    }

    return valid;
  }

  private boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
