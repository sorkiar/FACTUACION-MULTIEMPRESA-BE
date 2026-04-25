package com.api.multiempresa.dto.filter;

import lombok.Data;

@Data
public class ServiceFilter {
  private Long id;
  private Integer status;
  private Long serviceCategoryId;
  private String sku;
  private String name;
  /** PEN o USD — filtra servicios cuyo precio en esa moneda sea > 0 */
  private String currencyCode;
}
