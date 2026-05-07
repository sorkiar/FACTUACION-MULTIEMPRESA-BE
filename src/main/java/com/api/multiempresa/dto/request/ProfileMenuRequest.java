package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class ProfileMenuRequest {

  @NotNull(message = "Los permisos son obligatorios")
  private List<Long> menuIds;
}
