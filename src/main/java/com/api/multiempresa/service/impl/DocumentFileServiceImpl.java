package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.CreditDebitNote;
import com.api.multiempresa.dto.entity.Document;
import com.api.multiempresa.dto.entity.RemissionGuide;
import com.api.multiempresa.dto.response.FileDownload;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CreditDebitNoteRepository;
import com.api.multiempresa.repository.DocumentRepository;
import com.api.multiempresa.repository.RemissionGuideRepository;
import com.api.multiempresa.service.DocumentFileService;
import com.api.multiempresa.service.GoogleDriveService;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentFileServiceImpl implements DocumentFileService {

  private static final Pattern DRIVE_FILE_ID_PATTERN =
      Pattern.compile("drive\\.google\\.com/file/d/([^/]+)");

  private final DocumentRepository documentRepository;
  private final CreditDebitNoteRepository creditDebitNoteRepository;
  private final RemissionGuideRepository remissionGuideRepository;
  private final GoogleDriveService googleDriveService;

  // ============================================================================================
  // DOCUMENTS (facturas / boletas)
  // ============================================================================================

  @Override
  public FileDownload getDocumentXml(Long id) {
    Document doc = documentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con id: " + id));
    requireNotBlank(doc.getXmlBase64(), "XML no disponible para este documento.");
    String filename = doc.getSeries() + "-" + doc.getSequence() + ".xml";
    return new FileDownload(filename, "application/xml", decodeBase64(doc.getXmlBase64()));
  }

  @Override
  public FileDownload getDocumentCdr(Long id) {
    Document doc = documentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con id: " + id));
    requireNotBlank(doc.getCdrBase64(), "CDR no disponible para este documento.");
    String filename = "CDR-" + doc.getSeries() + "-" + doc.getSequence() + ".xml";
    return new FileDownload(filename, "application/xml", decodeBase64(doc.getCdrBase64()));
  }

  @Override
  public FileDownload getDocumentPdf(Long id) {
    Document doc = documentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con id: " + id));
    requireNotBlank(doc.getPdfUrl(), "PDF no disponible para este documento.");
    String filename = doc.getSeries() + "-" + doc.getSequence() + ".pdf";
    return new FileDownload(filename, "application/pdf", downloadFromDrive(doc.getPdfUrl()));
  }

  // ============================================================================================
  // CREDIT / DEBIT NOTES (notas de crédito / débito)
  // ============================================================================================

  @Override
  public FileDownload getCreditDebitNoteXml(Long id) {
    CreditDebitNote note = findNote(id);
    requireNotBlank(note.getXmlBase64(), "XML no disponible para esta nota.");
    String filename = note.getSeries() + "-" + note.getSequence() + ".xml";
    return new FileDownload(filename, "application/xml", decodeBase64(note.getXmlBase64()));
  }

  @Override
  public FileDownload getCreditDebitNoteCdr(Long id) {
    CreditDebitNote note = findNote(id);
    requireNotBlank(note.getCdrBase64(), "CDR no disponible para esta nota.");
    String filename = "CDR-" + note.getSeries() + "-" + note.getSequence() + ".xml";
    return new FileDownload(filename, "application/xml", decodeBase64(note.getCdrBase64()));
  }

  @Override
  public FileDownload getCreditDebitNotePdf(Long id) {
    CreditDebitNote note = findNote(id);
    requireNotBlank(note.getPdfUrl(), "PDF no disponible para esta nota.");
    String filename = note.getSeries() + "-" + note.getSequence() + ".pdf";
    return new FileDownload(filename, "application/pdf", downloadFromDrive(note.getPdfUrl()));
  }

  // ============================================================================================
  // REMISSION GUIDES (guías de remisión)
  // ============================================================================================

  @Override
  public FileDownload getRemissionGuideXml(Long id) {
    RemissionGuide guide = findGuide(id);
    requireNotBlank(guide.getXmlBase64(), "XML no disponible para esta guía.");
    String filename = guide.getSeries() + "-" + guide.getSequence() + ".xml";
    return new FileDownload(filename, "application/xml", decodeBase64(guide.getXmlBase64()));
  }

  @Override
  public FileDownload getRemissionGuideCdr(Long id) {
    RemissionGuide guide = findGuide(id);
    requireNotBlank(guide.getCdrBase64(), "CDR no disponible para esta guía.");
    String filename = "CDR-" + guide.getSeries() + "-" + guide.getSequence() + ".xml";
    return new FileDownload(filename, "application/xml", decodeBase64(guide.getCdrBase64()));
  }

  @Override
  public FileDownload getRemissionGuidePdf(Long id) {
    RemissionGuide guide = findGuide(id);
    requireNotBlank(guide.getPdfUrl(), "PDF no disponible para esta guía.");
    String filename = guide.getSeries() + "-" + guide.getSequence() + ".pdf";
    return new FileDownload(filename, "application/pdf", downloadFromDrive(guide.getPdfUrl()));
  }

  // ============================================================================================
  // HELPERS
  // ============================================================================================

  private CreditDebitNote findNote(Long id) {
    return creditDebitNoteRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada con id: " + id));
  }

  private RemissionGuide findGuide(Long id) {
    return remissionGuideRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ResourceNotFoundException("Guía no encontrada con id: " + id));
  }

  private byte[] decodeBase64(String base64) {
    return Base64.getDecoder().decode(base64);
  }

  private byte[] downloadFromDrive(String driveUrl) {
    String fileId = extractDriveFileId(driveUrl);
    try {
      return googleDriveService.downloadFileById(fileId);
    } catch (Exception e) {
      throw new BusinessValidationException("Error al descargar el archivo desde Drive: " + e.getMessage());
    }
  }

  private String extractDriveFileId(String driveUrl) {
    Matcher matcher = DRIVE_FILE_ID_PATTERN.matcher(driveUrl);
    if (matcher.find()) {
      return matcher.group(1);
    }
    throw new BusinessValidationException("URL de Drive inválida: " + driveUrl);
  }

  private void requireNotBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new BusinessValidationException(message);
    }
  }
}
