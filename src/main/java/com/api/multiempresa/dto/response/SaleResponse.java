package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SaleResponse {
  private Long id;
  private String saleStatus;
  private String currencyCode;
  private String paymentType;
  private String purchaseOrder;
  private String observations;
  private BigDecimal subtotalAmount;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
  private Boolean hasRetention;
  private BigDecimal retentionAmount;
  private BigDecimal retentionRate;
  private BigDecimal netAmount;
  private Boolean hasDetraction;
  private String detractionCode;
  private BigDecimal detractionRate;
  private BigDecimal detractionAmount;
  private List<String> relatedGuides;
  private ClientResponse client;
  private Long clientAddressId;
  private String clientAddress;
  private List<SaleItemResponse> items;
  private List<SalePaymentResponse> payments;
  private List<SaleInstallmentResponse> installments;
  private DocumentResponse document;
}
