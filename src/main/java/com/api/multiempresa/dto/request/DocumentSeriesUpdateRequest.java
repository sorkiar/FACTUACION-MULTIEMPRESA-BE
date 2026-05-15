package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DocumentSeriesUpdateRequest {

  @NotBlank(message = "series es obligatorio")
  @Size(min = 1, max = 4, message = "series debe tener entre 1 y 4 caracteres")
  private String series;
}
