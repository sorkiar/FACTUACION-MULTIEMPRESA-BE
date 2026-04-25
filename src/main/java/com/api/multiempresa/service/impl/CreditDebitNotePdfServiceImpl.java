package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Client;
import com.api.multiempresa.dto.entity.CreditDebitNote;
import com.api.multiempresa.dto.entity.CreditDebitNoteItem;
import com.api.multiempresa.dto.entity.Sale;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.dto.entity.ExchangeRate;
import com.api.multiempresa.repository.CreditDebitNoteItemRepository;
import com.api.multiempresa.repository.CreditDebitNoteRepository;
import com.api.multiempresa.repository.ExchangeRateRepository;
import com.api.multiempresa.service.ConfigurationService;
import com.api.multiempresa.service.CreditDebitNotePdfService;
import com.api.multiempresa.service.GoogleDriveService;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditDebitNotePdfServiceImpl implements CreditDebitNotePdfService {

  private final CreditDebitNoteRepository noteRepository;
  private final CreditDebitNoteItemRepository noteItemRepository;
  private final ExchangeRateRepository exchangeRateRepository;
  private final GoogleDriveService googleDriveService;
  private final ConfigurationService configurationService;

  @Value("${drive.folder-id.notas}")
  private String notasFolderId;

  @Override
  @Transactional
  public void generatePdf(Long noteId) {
    try {
      CreditDebitNote note = noteRepository.findByIdAndDeletedAtIsNull(noteId)
          .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada"));

      List<CreditDebitNoteItem> items =
          noteItemRepository.findByCreditDebitNoteIdAndDeletedAtIsNull(noteId);

      if (items.isEmpty()) {
        throw new BusinessValidationException("La nota no tiene ítems registrados");
      }

      String origDocTypeCode = note.getOriginalDocument().getDocumentTypeSunat().getCode();
      String docRefTipo = "01".equals(origDocTypeCode)
          ? "FACTURA ELECTRÓNICA"
          : "BOLETA ELECTRÓNICA";
      String docRef = note.getOriginalDocument().getSeries()
          + "-" + note.getOriginalDocument().getSequence();
      String tipoNotaDesc = note.getCreditDebitNoteType().getName();

      Map<String, String> config = configurationService.getGroup("empresa_emisora");

      List<Map<String, Object>> dataList = new ArrayList<>();

      for (CreditDebitNoteItem item : items) {

        Map<String, Object> row = new HashMap<>();

        // ================= EMPRESA =================
        row.put("empr_ruc", config.get("emprRuc"));
        row.put("empr_razon_social", config.get("emprRazonSocial"));
        row.put("empr_nombre_comercial", config.get("emprNombreComercial"));
        row.put("empr_direccion_fiscal", config.get("emprDireccionFiscal"));
        row.put("empr_telefono", config.get("emprTelefono"));
        row.put("empr_pagina_web", config.get("emprPaginaWeb"));
        row.put("empr_direccion_sucursal", null);
        row.put("empr_pdf_marca_agua", null);
        row.put("empr_pdf_texto_inferior", null);
        row.put("empr_pdf_eslogan", null);

        // ================= COMPROBANTE =================
        String tipoDocCode = note.getDocumentTypeSunat().getCode();
        row.put("tico_descripcion",
            "07".equals(tipoDocCode)
                ? "NOTA DE CRÉDITO ELECTRÓNICA"
                : "NOTA DE DÉBITO ELECTRÓNICA");

        row.put("comp_numero_comprobante",
            note.getSeries() + "-" + note.getSequence());
        row.put("comp_fecha_emicion", Timestamp.valueOf(LocalDateTime.now()));
        row.put("comp_estado", "ACTIVO");
        row.put("comp_descuento_global", 0);

        // ================= CLIENTE =================
        Client client = note.getSale().getClient();
        row.put("comp_descripcion_cliente", resolveClientName(origDocTypeCode, client));
        row.put("clie_numero_documento", client.getDocumentNumber());
        row.put("comp_direccion_cliente", note.getSale().getClientAddress());
        row.put("comp_condicion_pago", "Contado");
        row.put("priorizar_despacho", false);

        // ================= NOTA DE REFERENCIA =================
        row.put("doc_referencia", docRef);
        row.put("doc_referencia_tipo", docRefTipo);
        row.put("tipo_nota_desc", tipoNotaDesc);
        row.put("observaciones", note.getReason());

        // ================= MONEDA =================
        row.put("comp_descripcion_moneda", note.getCurrencyCode().equalsIgnoreCase("PEN") ? "SOLES" : "DÓLARES");
        row.put("comp_simbolo_moneda", note.getCurrencyCode().equalsIgnoreCase("PEN") ? "S/" : "$");

        // ================= ITEM =================
        String sku;
        if (item.getProduct() != null) {
          sku = item.getProduct().getSku();
        } else if (item.getService() != null) {
          sku = item.getService().getSku();
        } else {
          sku = "SRV0000000";
        }

        row.put("itco_codigo_interno", sku);
        row.put("itco_descripcion_completa", item.getDescription());
        row.put("itco_cantidad", item.getQuantity());

        String unidad = "NIU";
        if (item.getUnitMeasure() != null && item.getUnitMeasure().getCodeSunat() != null) {
          unidad = item.getUnitMeasure().getCodeSunat();
        }
        row.put("itco_unidad_medida", unidad);
        row.put("itco_precio_unitario", item.getUnitPrice());
        BigDecimal discountPct = item.getDiscountPercentage() != null
            ? item.getDiscountPercentage() : BigDecimal.ZERO;
        BigDecimal grossItemTotal = item.getQuantity()
            .multiply(item.getUnitPrice())
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = grossItemTotal
            .multiply(discountPct)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        row.put("itco_descuento", discountAmount);
        row.put("itco_tipo_igv", 10);
        row.put("itco_igv", item.getTaxAmount());
        row.put("itco_base", item.getSubtotalAmount());
        row.put("itco_importe", item.getTotalAmount());

        // ================= TRIBUTOS =================
        row.put("otros_tributos", 0.0);
        row.put("icbper", 0.0);

        // ================= QR =================
        row.put("comp_cadena_qr", buildQrString(note, config.get("emprRuc")));
        row.put("comp_codigo_hash", "");

        dataList.add(row);
      }

      // Compilar Jasper
      InputStream inputStream =
          getClass().getResourceAsStream("/jasper/NotaCreditoDebitoA4.jrxml");

      JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
      JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("urlImagen",
          Objects.requireNonNull(getClass().getResource("/img/logo.png")).toString());
      parameters.put("comp_total_amount", note.getTotalAmount());

      BigDecimal tipoConversion = BigDecimal.ONE;
      if ("USD".equalsIgnoreCase(note.getCurrencyCode())) {
        tipoConversion = exchangeRateRepository
            .findByDateAndType(note.getIssueDate().toLocalDate(), "V")
            .map(ExchangeRate::getValue)
            .orElse(BigDecimal.ONE);
      }
      parameters.put("comp_tipo_cambio", tipoConversion);
      parameters.put("comp_subtotal_amount", note.getSubtotalAmount());

      JasperPrint jasperPrint =
          JasperFillManager.fillReport(jasperReport, parameters, dataSource);

      // Nombre archivo
      String fileName = config.get("emprRuc") + "-"
          + note.getDocumentTypeSunat().getCode() + "-"
          + note.getSeries() + "-"
          + note.getSequence() + ".pdf";

      File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
      JasperExportManager.exportReportToPdfFile(jasperPrint, tempFile.getAbsolutePath());

      // Subir a Drive
      String fileId = googleDriveService.uploadPdf(tempFile, notasFolderId);
      String driveUrl = "https://drive.google.com/file/d/" + fileId + "/view";

      note.setPdfUrl(driveUrl);
      noteRepository.save(note);

      tempFile.delete();

    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessValidationException(
          "Error al generar el PDF de la nota: " + e.getMessage());
    }
  }

  private String resolveClientName(String origDocTypeCode, Client client) {
    if ("01".equals(origDocTypeCode)) {
      if (client.getBusinessName() == null || client.getBusinessName().isBlank()) {
        throw new IllegalStateException(
            "El cliente no tiene razón social para emitir factura");
      }
      return client.getBusinessName();
    }
    String fullName =
        (client.getFirstName() != null ? client.getFirstName() : "") +
            " " +
            (client.getLastName() != null ? client.getLastName() : "");
    return fullName.trim();
  }

  private String buildQrString(CreditDebitNote note, String companyRuc) {
    Sale sale = note.getSale();
    return companyRuc + "|" +
        note.getDocumentTypeSunat().getCode() + "|" +
        note.getSeries() + "|" +
        note.getSequence() + "|" +
        note.getTaxAmount() + "|" +
        note.getTotalAmount() + "|" +
        note.getIssueDate().toLocalDate() + "|" +
        sale.getClient().getDocumentType().getName() + "|" +
        sale.getClient().getDocumentNumber();
  }
}
