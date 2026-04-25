package com.api.multiempresa.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CreditDebitNoteResponse {
  private Long id;
  private SaleResponse sale;
  private DocumentResponse originalDocument;

  private CreditDebitNoteTypeResponse creditDebitNoteType;
  private String reason;

  private String series;
  private String sequence;
  private LocalDateTime issueDate;

  private BigDecimal subtotalAmount;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
  private BigDecimal taxPercentage;
  private String currencyCode;

  private String status;
  private Integer sunatResponseCode;
  private String sunatMessage;
  private String hashCode;
  private String qrCode;
  private String pdfUrl;
  private String xmlUrl;
  private String cdrUrl;

  private List<CreditDebitNoteItemResponse> items;
}
