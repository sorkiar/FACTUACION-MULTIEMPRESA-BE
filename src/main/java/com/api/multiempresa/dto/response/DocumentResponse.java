package com.api.multiempresa.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DocumentResponse {
  private Long id;
  private String series;
  private String sequence;
  private LocalDateTime issueDate;
  private String status;
  private Integer sunatResponseCode;
  private String sunatMessage;
  private String pdfUrl;
  private String xmlUrl;
  private String cdrUrl;
}
