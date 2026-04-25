package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.FileDownload;

public interface DocumentFileService {
  FileDownload getDocumentXml(Long id);
  FileDownload getDocumentCdr(Long id);
  FileDownload getDocumentPdf(Long id);

  FileDownload getCreditDebitNoteXml(Long id);
  FileDownload getCreditDebitNoteCdr(Long id);
  FileDownload getCreditDebitNotePdf(Long id);

  FileDownload getRemissionGuideXml(Long id);
  FileDownload getRemissionGuideCdr(Long id);
  FileDownload getRemissionGuidePdf(Long id);
}
