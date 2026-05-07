package com.api.multiempresa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MenuRequest {

  @NotBlank(message = "El nombre es obligatorio")
  private String name;

  private String path;

  private Long parentId;

  @NotNull(message = "El orden es obligatorio")
  private Integer sortOrder;

  @Pattern(regexp = "SIDEBAR|NAVBAR|INTERNAL",
      message = "menuType debe ser SIDEBAR, NAVBAR o INTERNAL")
  private String menuType;
}
