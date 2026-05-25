package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Client;
import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.entity.Document;
import com.api.multiempresa.dto.entity.Sale;
import com.api.multiempresa.dto.entity.SaleInstallment;
import com.api.multiempresa.dto.entity.SaleItem;
import com.api.multiempresa.dto.entity.SaleRelatedGuide;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.dto.entity.ExchangeRate;
import com.api.multiempresa.repository.DetractionCodeRepository;
import com.api.multiempresa.repository.DocumentRepository;
import com.api.multiempresa.repository.ExchangeRateRepository;
import com.api.multiempresa.repository.SaleInstallmentRepository;
import com.api.multiempresa.repository.SaleItemRepository;
import com.api.multiempresa.repository.SaleRelatedGuideRepository;
import com.api.multiempresa.repository.SaleRepository;
import com.api.multiempresa.service.ConfigurationService;
import com.api.multiempresa.service.DocumentPdfService;
import com.api.multiempresa.service.GoogleDriveService;
import com.api.multiempresa.util.PdfLogoResolver;
import jakarta.transaction.Transactional;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentPdfServiceImpl implements DocumentPdfService {

  private final SaleRepository saleRepository;
  private final DocumentRepository documentRepository;
  private final SaleItemRepository saleItemRepository;
  private final SaleInstallmentRepository saleInstallmentRepository;
  private final SaleRelatedGuideRepository saleRelatedGuideRepository;
  private final DetractionCodeRepository detractionCodeRepository;
  private final ExchangeRateRepository exchangeRateRepository;
  private final GoogleDriveService googleDriveService;
  private final ConfigurationService configurationService;
  private final PdfLogoResolver pdfLogoResolver;

  @Value("${drive.folder-id.boletas}")
  private String boletasFolderId;

  @Value("${drive.folder-id.facturas}")
  private String facturasFolderId;

  @Override
  @Transactional
  public void generatePdf(Long saleId) {
    try {
      Sale sale = saleRepository.findById(saleId)
          .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

      Document document = documentRepository.findBySaleId(saleId)
          .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));

      // ================================
      // Construir datos del reporte
      // ================================

      Map<String, String> config = new java.util.HashMap<>(
          configurationService.getGroup("empresa_emisora"));
      Company company = document.getCompany();
      if (company != null) {
        config.put("emprRuc", company.getRuc());
        config.put("emprRazonSocial", company.getBusinessName());
        config.put("emprNombreComercial", company.getTradeName());
        config.put("emprDireccionFiscal", company.getAddress());
        config.put("emprTelefono", company.getPhone());
        config.put("emprPaginaWeb", company.getWebsite());
      }

      List<Map<String, Object>> dataList = new ArrayList<>();

      List<SaleItem> items = saleItemRepository.findBySaleId(saleId);

      if (items.isEmpty()) {
        throw new BusinessValidationException("La venta no tiene ítems registrados");
      }

      for (SaleItem item : items) {

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
        row.put("tico_descripcion",
            document.getDocumentTypeSunat().getCode().equals("01")
                ? "FACTURA ELECTRÓNICA"
                : "BOLETA ELECTRÓNICA");

        row.put("comp_numero_comprobante",
            document.getSeries() + "-" + document.getSequence());

        row.put("comp_fecha_emicion",
            Timestamp.valueOf(LocalDateTime.now()));

        row.put("comp_estado", "ACTIVO");
        row.put("comp_descuento_global", 0);

        row.put("comp_descripcion_cliente",
            resolveClientName(document, sale.getClient()));

        row.put("clie_numero_documento",
            sale.getClient().getDocumentNumber());

        row.put("comp_direccion_cliente", sale.getClientAddress());

        row.put("comp_condicion_pago",
            sale.getPaymentType() != null ? sale.getPaymentType() : "CONTADO");

        row.put("priorizar_despacho", false);
        row.put("observaciones", sale.getObservations());

        // ================= MONEDA =================
        row.put("comp_descripcion_moneda", sale.getCurrencyCode().equalsIgnoreCase("PEN") ? "SOLES" : "DÓLARES");
        row.put("comp_simbolo_moneda", sale.getCurrencyCode().equalsIgnoreCase("PEN") ? "S/" : "$");

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

        if (item.getUnitMeasure() != null &&
            item.getUnitMeasure().getCodeSunat() != null) {

          unidad = item.getUnitMeasure().getCodeSunat();
        }

        row.put("itco_unidad_medida", unidad);
        row.put("itco_precio_unitario", item.getUnitPrice());
        BigDecimal discountPct = item.getDiscountPercentage() != null
            ? item.getDiscountPercentage() : BigDecimal.ZERO;
        BigDecimal grossTotal = item.getQuantity().multiply(item.getUnitPrice())
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = grossTotal.multiply(discountPct)
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
        row.put("comp_cadena_qr", buildQrString(document, sale, config.get("emprRuc")));
        row.put("comp_codigo_hash", "");

        dataList.add(row);
      }

      // ================================
      // Compilar Jasper
      // ================================

      String template = document.getDocumentTypeSunat().getCode().equalsIgnoreCase("01")
          ? "/jasper/FacturaA4.jrxml"
          : "/jasper/BoletaA4.jrxml";

      InputStream inputStream =
          getClass().getResourceAsStream(template);

      JasperReport jasperReport =
          JasperCompileManager.compileReport(inputStream);

      JRBeanCollectionDataSource dataSource =
          new JRBeanCollectionDataSource(dataList);

      // ================================
      // Construir texto de cuotas (solo CREDITO)
      // ================================

      String cuotasTexto = null;
      if ("CREDITO".equals(sale.getPaymentType())) {
        List<SaleInstallment> installments =
            saleInstallmentRepository
                .findBySaleIdAndDeletedAtIsNullOrderByInstallmentNumberAsc(saleId);
        if (!installments.isEmpty()) {
          String symbol = "USD".equalsIgnoreCase(sale.getCurrencyCode()) ? "$" : "S/";
          DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
          StringBuilder sb = new StringBuilder();
          sb.append(String.format("%-4s  %-12s  %s%n", "N°", "Vencimiento", "Importe"));
          for (SaleInstallment inst : installments) {
            sb.append(String.format("%-4d  %-12s  %s %,.2f%n",
                inst.getInstallmentNumber(),
                inst.getDueDate().format(fmt),
                symbol,
                inst.getAmount()));
          }
          cuotasTexto = sb.toString().trim();
        }
      }

      // ================================
      // Construir parámetros: guías relacionadas y orden de compra
      // ================================

      List<SaleRelatedGuide> relatedGuides =
          saleRelatedGuideRepository.findBySaleId(saleId);
      String guiasTexto = relatedGuides.isEmpty() ? null :
          relatedGuides.stream()
              .map(SaleRelatedGuide::getGuideNumber)
              .collect(Collectors.joining(", "));

      Map<String, Object> parameters = new HashMap<>();

      parameters.put("urlImagen", pdfLogoResolver.resolveLogoUrl(company));
      parameters.put("cuotas_texto", cuotasTexto);
      parameters.put("guias_relacionadas", guiasTexto);
      parameters.put("orden_compra", sale.getPurchaseOrder());
      parameters.put("comp_total_amount", sale.getTotalAmount());
      parameters.put("has_retention", Boolean.TRUE.equals(sale.getHasRetention()));
      parameters.put("retention_rate", sale.getRetentionRate());
      // retention_amount ya está en PEN; retention_base = retentionAmount * 100 / rate (base en PEN)
      parameters.put("retention_amount", sale.getRetentionAmount());
      BigDecimal retentionBase = (sale.getRetentionAmount() != null && sale.getRetentionRate() != null
          && sale.getRetentionRate().compareTo(java.math.BigDecimal.ZERO) != 0)
          ? sale.getRetentionAmount()
              .multiply(new java.math.BigDecimal("100"))
              .divide(sale.getRetentionRate(), 2, java.math.RoundingMode.HALF_UP)
          : sale.getTotalAmount();
      parameters.put("retention_base", retentionBase);

      boolean hasDetraction = Boolean.TRUE.equals(sale.getHasDetraction());
      parameters.put("has_detraction", hasDetraction);
      parameters.put("detraction_code", sale.getDetractionCode());
      parameters.put("detraction_rate", sale.getDetractionRate());
      parameters.put("detraction_amount", sale.getDetractionAmount());
      String detrDescription = "";
      String detrAccount = "";
      if (hasDetraction) {
        if (sale.getDetractionCode() != null) {
          detrDescription = detractionCodeRepository.findByCode(sale.getDetractionCode())
              .map(dc -> dc.getDescription())
              .orElse("");
        }
        Map<String, String> detrConfig = configurationService.getGroup("detraccion_retencion");
        detrAccount = detrConfig.getOrDefault("banco_nacion_detraccion", "");
      }
      parameters.put("detraction_description", detrDescription);
      parameters.put("detraction_account", detrAccount);

      BigDecimal tipoConversion = BigDecimal.ONE;
      if ("USD".equalsIgnoreCase(sale.getCurrencyCode())) {
        tipoConversion = exchangeRateRepository
            .findByDateAndType(sale.getSaleDate().toLocalDate(), "V")
            .map(ExchangeRate::getValue)
            .orElse(BigDecimal.ONE);
      }
      parameters.put("comp_tipo_cambio", tipoConversion);
      parameters.put("comp_subtotal_amount", sale.getSubtotalAmount());

      JasperPrint jasperPrint =
          JasperFillManager.fillReport(
              jasperReport,
              parameters,
              dataSource
          );

      // ================================
      // 3Nombre archivo correcto
      // ================================

      String fileName =
          config.get("emprRuc") + "-" +
              document.getDocumentTypeSunat().getCode() + "-" +
              document.getSeries() + "-" +
              document.getSequence() + ".pdf";

      File tempFile = new File(
          System.getProperty("java.io.tmpdir") + "/" + fileName);

      JasperExportManager.exportReportToPdfFile(
          jasperPrint,
          tempFile.getAbsolutePath()
      );

      // ================================
      // Subir a Drive
      // ================================

      String folderId = "01".equals(document.getDocumentTypeSunat().getCode())
          ? facturasFolderId : boletasFolderId;
      String fileId =
          googleDriveService.uploadPdf(tempFile, folderId);

      String driveUrl =
          "https://drive.google.com/file/d/" + fileId + "/view";

      document.setPdfUrl(driveUrl);
      documentRepository.save(document);

      tempFile.delete();
    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessValidationException(
          "Error al generar el PDF del comprobante: " + e.getMessage()
      );
    }
  }

  private String resolveClientName(Document document, Client client) {

    String documentType = document.getDocumentTypeSunat().getCode();

    // FACTURA
    if ("01".equals(documentType)) {

      if (client.getBusinessName() == null || client.getBusinessName().isBlank()) {
        throw new IllegalStateException(
            "El cliente no tiene razón social para emitir factura"
        );
      }

      return client.getBusinessName();
    }

    // BOLETA
    if ("03".equals(documentType)) {

      String fullName =
          (client.getFirstName() != null ? client.getFirstName() : "") +
              " " +
              (client.getLastName() != null ? client.getLastName() : "");

      return fullName.trim();
    }

    return "";
  }

  private String buildQrString(Document document, Sale sale, String companyRuc) {

    if (document.getDocumentTypeSunat() == null) return "";

    return companyRuc + "|" +
        document.getDocumentTypeSunat().getCode() + "|" +
        document.getSeries() + "|" +
        document.getSequence() + "|" +
        sale.getTaxAmount() + "|" +
        sale.getTotalAmount() + "|" +
        sale.getSaleDate().toLocalDate() + "|" +
        sale.getClient().getDocumentType().getName() + "|" +
        sale.getClient().getDocumentNumber();
  }

}
