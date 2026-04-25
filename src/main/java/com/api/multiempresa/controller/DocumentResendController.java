package com.api.multiempresa.controller;

import com.api.multiempresa.dto.external.FacturacionResponse;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.FileDownload;
import com.api.multiempresa.dto.response.SunatDocumentSummaryResponse;
import com.api.multiempresa.job.SunatDocumentJobService;
import com.api.multiempresa.service.DocumentFileService;
import com.api.multiempresa.service.DocumentResendService;
import com.api.multiempresa.service.SunatDocumentListService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentResendController {

  private final DocumentResendService resendService;
  private final DocumentFileService fileService;
  private final SunatDocumentListService listService;
  private final SunatDocumentJobService jobService;

  /**
   * Reenvía manualmente una factura o boleta que tenga estado ERROR o RECHAZADO.
   * Resetea el estado a PENDIENTE para que el job lo reprocese.
   */
  @PostMapping("/documents/{id}/resend")
  public ApiResponse<String> resendDocument(@PathVariable Long id) {
    return resendService.resendDocument(id);
  }

  /**
   * Reenvía manualmente una nota de crédito o débito que tenga estado ERROR o RECHAZADO.
   * Resetea el estado a PENDIENTE para que el job lo reprocese.
   */
  @PostMapping("/credit-debit-notes/{id}/resend")
  public ApiResponse<String> resendCreditDebitNote(@PathVariable Long id) {
    return resendService.resendCreditDebitNote(id);
  }

  /**
   * Reenvía manualmente una guía de remisión que tenga estado ERROR o RECHAZADO.
   * Resetea el estado a PENDIENTE para que el job lo reprocese.
   */
  @PostMapping("/remission-guides/{id}/resend")
  public ApiResponse<String> resendRemissionGuide(@PathVariable Long id) {
    return resendService.resendRemissionGuide(id);
  }

  // ========================================================================================
  // REGENERAR PDF
  // ========================================================================================

  @PostMapping("/documents/{id}/regenerate-pdf")
  public ApiResponse<String> regenerateDocumentPdf(@PathVariable Long id) {
    return resendService.regenerateDocumentPdf(id);
  }

  @PostMapping("/credit-debit-notes/{id}/regenerate-pdf")
  public ApiResponse<String> regenerateCreditDebitNotePdf(@PathVariable Long id) {
    return resendService.regenerateCreditDebitNotePdf(id);
  }

  @PostMapping("/remission-guides/{id}/regenerate-pdf")
  public ApiResponse<String> regenerateRemissionGuidePdf(@PathVariable Long id) {
    return resendService.regenerateRemissionGuidePdf(id);
  }

  // ========================================================================================
  // LISTADO UNIFICADO SUNAT
  // ========================================================================================

  /**
   * Lista todos los comprobantes electrónicos (facturas, boletas, notas de crédito/débito
   * y guías de remisión) ordenados por fecha de emisión descendente.
   *
   * @param status filtro opcional: PENDIENTE | ACEPTADO | RECHAZADO | ERROR
   */
  @GetMapping("/sunat/documents")
  public ApiResponse<List<SunatDocumentSummaryResponse>> listSunatDocuments(
      @RequestParam(required = false) String status) {
    return new ApiResponse<>(null, listService.listAll(status));
  }

  // ========================================================================================
  // DESCARGA DE ARCHIVOS — DOCUMENTOS (facturas / boletas)
  // ========================================================================================

  @GetMapping("/documents/{id}/xml")
  public ResponseEntity<byte[]> getDocumentXml(@PathVariable Long id) {
    return toFileResponse(fileService.getDocumentXml(id));
  }

  @GetMapping("/documents/{id}/cdr")
  public ResponseEntity<byte[]> getDocumentCdr(@PathVariable Long id) {
    return toFileResponse(fileService.getDocumentCdr(id));
  }

  @GetMapping("/documents/{id}/pdf")
  public ResponseEntity<byte[]> getDocumentPdf(@PathVariable Long id) {
    return toFileResponse(fileService.getDocumentPdf(id));
  }

  // ========================================================================================
  // DESCARGA DE ARCHIVOS — NOTAS DE CRÉDITO / DÉBITO
  // ========================================================================================

  @GetMapping("/credit-debit-notes/{id}/xml")
  public ResponseEntity<byte[]> getCreditDebitNoteXml(@PathVariable Long id) {
    return toFileResponse(fileService.getCreditDebitNoteXml(id));
  }

  @GetMapping("/credit-debit-notes/{id}/cdr")
  public ResponseEntity<byte[]> getCreditDebitNoteCdr(@PathVariable Long id) {
    return toFileResponse(fileService.getCreditDebitNoteCdr(id));
  }

  @GetMapping("/credit-debit-notes/{id}/pdf")
  public ResponseEntity<byte[]> getCreditDebitNotePdf(@PathVariable Long id) {
    return toFileResponse(fileService.getCreditDebitNotePdf(id));
  }

  // ========================================================================================
  // DESCARGA DE ARCHIVOS — GUÍAS DE REMISIÓN
  // ========================================================================================

  @GetMapping("/remission-guides/{id}/xml")
  public ResponseEntity<byte[]> getRemissionGuideXml(@PathVariable Long id) {
    return toFileResponse(fileService.getRemissionGuideXml(id));
  }

  @GetMapping("/remission-guides/{id}/cdr")
  public ResponseEntity<byte[]> getRemissionGuideCdr(@PathVariable Long id) {
    return toFileResponse(fileService.getRemissionGuideCdr(id));
  }

  @GetMapping("/remission-guides/{id}/pdf")
  public ResponseEntity<byte[]> getRemissionGuidePdf(@PathVariable Long id) {
    return toFileResponse(fileService.getRemissionGuidePdf(id));
  }

  // ========================================================================================
  // JOB MANUAL
  // ========================================================================================

  /**
   * Consulta el ticket de una guía por su ID, actualiza su estado en base de datos
   * y devuelve la respuesta del facturador. Requiere que la guía tenga ticket registrado.
   */
  @PostMapping("/remission-guides/{id}/check-ticket")
  public ApiResponse<FacturacionResponse> checkGuideTicketById(@PathVariable Long id) {
    return new ApiResponse<>(null, jobService.checkGuideTicketById(id));
  }

  /**
   * Consulta el estado de un ticket de guía de remisión en el facturador externo.
   * Los datos de la empresa se obtienen automáticamente de la configuración.
   * No actualiza ningún registro — solo retorna la respuesta del facturador.
   */
  @PostMapping("/remission-guides/check-ticket")
  public ApiResponse<FacturacionResponse> checkGuideTicket(@RequestParam String ticket) {
    return new ApiResponse<>(null, jobService.checkGuideTicket(ticket));
  }

  /**
   * Dispara manualmente el job de envío a SUNAT para procesar todos los documentos
   * y notas en estado PENDIENTE de forma inmediata, sin esperar el ciclo programado.
   */
  @PostMapping("/admin/trigger-sunat-job")
  public ApiResponse<String> triggerSunatJob() {
    jobService.scheduledTick();
    return new ApiResponse<>("Job de envío a SUNAT ejecutado correctamente.", null);
  }

  // ========================================================================================
  // HELPER
  // ========================================================================================

  private ResponseEntity<byte[]> toFileResponse(FileDownload file) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
        .contentType(MediaType.parseMediaType(file.contentType()))
        .body(file.content());
  }
}
