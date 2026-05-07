package com.api.multiempresa.dto.response;

import lombok.Data;

@Data
public class MenuResponse {
  private Long id;
  private String name;
  private String path;
  private Long parentId;
  private Integer sortOrder;
  private Integer status;
  private String menuType;
}
