package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class PersonTypeResponse {
  private Long id;
  private String name;
  private String description;
  private Integer status;
}
