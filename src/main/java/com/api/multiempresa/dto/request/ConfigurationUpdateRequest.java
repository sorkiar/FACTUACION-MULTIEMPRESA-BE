package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfigurationUpdateRequest {

  private String configValue;

  private String description;

  @NotNull
  @Min(0)
  @Max(1)
  private Integer editable;
}
