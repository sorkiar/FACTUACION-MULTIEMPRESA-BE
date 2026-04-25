package com.api.multiempresa.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConfigurationResponse {

  private Long id;
  private String configGroup;
  private String configKey;
  private String configValue;
  private String configDatatype;
  private String description;
  private Integer editable;
  private Integer sortOrder;
  private Integer colSpan;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
