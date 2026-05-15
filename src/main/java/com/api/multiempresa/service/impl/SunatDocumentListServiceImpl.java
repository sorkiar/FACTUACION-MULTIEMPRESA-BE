package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.CreditDebitNote;
import com.api.multiempresa.dto.entity.Document;
import com.api.multiempresa.dto.entity.RemissionGuide;
import com.api.multiempresa.dto.response.SunatDocumentSummaryResponse;
import com.api.multiempresa.repository.CreditDebitNoteRepository;
import com.api.multiempresa.repository.DocumentRepository;
import com.api.multiempresa.repository.RemissionGuideRepository;
import com.api.multiempresa.service.SunatDocumentListService;
import com.api.multiempresa.util.TenantContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SunatDocumentListServiceImpl implements SunatDocumentListService {

  private final DocumentRepository documentRepository;
  private final CreditDebitNoteRepository creditDebitNoteRepository;
  private final RemissionGuideRepository remissionGuideRepository;

  @Override
  @Transactional(readOnly = true)
  public List<SunatDocumentSummaryResponse> listAll(String status) {

    Long companyId = TenantContext.getCurrentCompanyId();
    boolean hasStatus = status != null && !status.isBlank();
    List<SunatDocumentSummaryResponse> result = new ArrayList<>();

    // Facturas y boletas
    List<Document> docs;
    List<CreditDebitNote> notes;
    List<RemissionGuide> guides;

    if (companyId != null) {
      docs = hasStatus
          ? documentRepository.findByCompany_IdAndStatusAndDeletedAtIsNullOrderByIssueDateDesc(companyId, status)
          : documentRepository.findByCompany_IdAndDeletedAtIsNullOrderByIssueDateDesc(companyId);
      notes = hasStatus
          ? creditDebitNoteRepository.findByCompany_IdAndStatusAndDeletedAtIsNullOrderByIssueDateDesc(companyId, status)
          : creditDebitNoteRepository.findByCompany_IdAndDeletedAtIsNullOrderByIssueDateDesc(companyId);
      guides = hasStatus
          ? remissionGuideRepository.findByCompany_IdAndStatusAndDeletedAtIsNullOrderByIssueDateDesc(companyId, status)
          : remissionGuideRepository.findByCompany_IdAndDeletedAtIsNullOrderByIssueDateDesc(companyId);
    } else {
      docs = hasStatus
          ? documentRepository.findByStatusAndDeletedAtIsNullOrderByIssueDateDesc(status)
          : documentRepository.findByDeletedAtIsNullOrderByIssueDateDesc();
      notes = hasStatus
          ? creditDebitNoteRepository.findByStatusAndDeletedAtIsNullOrderByIssueDateDesc(status)
          : creditDebitNoteRepository.findByDeletedAtIsNullOrderByIssueDateDesc();
      guides = hasStatus
          ? remissionGuideRepository.findByStatusAndDeletedAtIsNullOrderByIssueDateDesc(status)
          : remissionGuideRepository.findByDeletedAtIsNullOrderByIssueDateDesc();
    }

    docs.forEach(doc -> result.add(fromDocument(doc)));
    notes.forEach(note -> result.add(fromNote(note)));
    guides.forEach(guide -> result.add(fromGuide(guide)));

    result.sort(Comparator.comparing(
        SunatDocumentSummaryResponse::getIssueDate,
        Comparator.nullsLast(Comparator.reverseOrder())
    ));

    return result;
  }

  // ========================================================================================
  // MAPPERS
  // ========================================================================================

  private SunatDocumentSummaryResponse fromDocument(Document doc) {
    String typeCode = doc.getDocumentTypeSunat() != null
        ? doc.getDocumentTypeSunat().getCode() : null;
    String typeName = doc.getDocumentTypeSunat() != null
        ? doc.getDocumentTypeSunat().getName() : null;

    return SunatDocumentSummaryResponse.builder()
        .id(doc.getId())
        .category("DOCUMENTO")
        .documentTypeCode(typeCode)
        .documentTypeName(typeName)
        .series(doc.getSeries())
        .sequence(doc.getSequence())
        .voucherNumber(doc.getSeries() + "-" + doc.getSequence())
        .issueDate(doc.getIssueDate())
        .status(doc.getStatus())
        .sunatResponseCode(doc.getSunatResponseCode())
        .sunatMessage(doc.getSunatMessage())
        .hashCode(doc.getHashCode())
        .hasXml(doc.getXmlBase64() != null && !doc.getXmlBase64().isBlank())
        .hasCdr(doc.getCdrBase64() != null && !doc.getCdrBase64().isBlank())
        .hasPdf(doc.getPdfUrl() != null && !doc.getPdfUrl().isBlank())
        .pdfUrl(doc.getPdfUrl())
        .xmlUrl(doc.getXmlUrl())
        .cdrUrl(doc.getCdrUrl())
        .build();
  }

  private SunatDocumentSummaryResponse fromNote(CreditDebitNote note) {
    String typeCode = note.getDocumentTypeSunat() != null
        ? note.getDocumentTypeSunat().getCode() : null;
    String typeName = note.getDocumentTypeSunat() != null
        ? note.getDocumentTypeSunat().getName() : null;

    String noteCode = note.getCreditDebitNoteType() != null
        ? note.getCreditDebitNoteType().getCode() : null;
    String noteCategory = note.getCreditDebitNoteType() != null
        ? note.getCreditDebitNoteType().getNoteCategory() : null;
    String noteTypeName = note.getCreditDebitNoteType() != null
        ? note.getCreditDebitNoteType().getName() : null;

    String originalVoucher = note.getOriginalDocument() != null
        ? note.getOriginalDocument().getSeries() + "-" + note.getOriginalDocument().getSequence()
        : null;

    return SunatDocumentSummaryResponse.builder()
        .id(note.getId())
        .category("NOTA")
        .documentTypeCode(typeCode)
        .documentTypeName(typeName)
        .series(note.getSeries())
        .sequence(note.getSequence())
        .voucherNumber(note.getSeries() + "-" + note.getSequence())
        .issueDate(note.getIssueDate())
        .status(note.getStatus())
        .sunatResponseCode(note.getSunatResponseCode())
        .sunatMessage(note.getSunatMessage())
        .hashCode(note.getHashCode())
        .hasXml(note.getXmlBase64() != null && !note.getXmlBase64().isBlank())
        .hasCdr(note.getCdrBase64() != null && !note.getCdrBase64().isBlank())
        .hasPdf(note.getPdfUrl() != null && !note.getPdfUrl().isBlank())
        .pdfUrl(note.getPdfUrl())
        .xmlUrl(note.getXmlUrl())
        .cdrUrl(note.getCdrUrl())
        .noteCode(noteCode)
        .noteCategory(noteCategory)
        .noteTypeName(noteTypeName)
        .originalVoucher(originalVoucher)
        .build();
  }

  private SunatDocumentSummaryResponse fromGuide(RemissionGuide guide) {
    return SunatDocumentSummaryResponse.builder()
        .id(guide.getId())
        .category("GUIA")
        .documentTypeCode("09")
        .documentTypeName("GUIA_REMISION_REMITENTE")
        .series(guide.getSeries())
        .sequence(guide.getSequence())
        .voucherNumber(guide.getSeries() + "-" + guide.getSequence())
        .issueDate(guide.getIssueDate())
        .status(guide.getStatus())
        .sunatResponseCode(guide.getSunatResponseCode())
        .sunatMessage(guide.getSunatMessage())
        .hashCode(guide.getHashCode())
        .hasXml(guide.getXmlBase64() != null && !guide.getXmlBase64().isBlank())
        .hasCdr(guide.getCdrBase64() != null && !guide.getCdrBase64().isBlank())
        .hasPdf(guide.getPdfUrl() != null && !guide.getPdfUrl().isBlank())
        .pdfUrl(guide.getPdfUrl())
        .xmlUrl(guide.getXmlUrl())
        .cdrUrl(guide.getCdrUrl())
        .transferReason(guide.getTransferReason().getName())
        .transferDate(guide.getTransferDate())
        .transportMode(guide.getTransportMode())
        .build();
  }
}
