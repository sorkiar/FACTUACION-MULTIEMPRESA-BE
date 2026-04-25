package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.ApiResponse;

public interface DocumentResendService {
  ApiResponse<String> resendDocument(Long id);
  ApiResponse<String> resendCreditDebitNote(Long id);
  ApiResponse<String> resendRemissionGuide(Long id);

  ApiResponse<String> regenerateDocumentPdf(Long id);
  ApiResponse<String> regenerateCreditDebitNotePdf(Long id);
  ApiResponse<String> regenerateRemissionGuidePdf(Long id);
}
