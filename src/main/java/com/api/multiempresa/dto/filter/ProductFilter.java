package com.api.multiempresa.dto.filter;

import lombok.Data;

@Data
public class ProductFilter {
  private Long id;
  private Integer status;
  private Long categoryId;
  private String sku;
  /** PEN o USD — filtra productos cuyo precio de venta en esa moneda sea > 0 */
  private String currencyCode;
}
