package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.RemissionGuide;
import com.api.multiempresa.dto.entity.RemissionGuideDriver;
import com.api.multiempresa.dto.entity.RemissionGuideItem;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.RemissionGuideDriverRepository;
import com.api.multiempresa.repository.RemissionGuideItemRepository;
import com.api.multiempresa.repository.RemissionGuideRepository;
import com.api.multiempresa.service.ConfigurationService;
import com.api.multiempresa.service.GoogleDriveService;
import com.api.multiempresa.service.RemissionGuidePdfService;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
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
public class RemissionGuidePdfServiceImpl implements RemissionGuidePdfService {

  private final RemissionGuideRepository guideRepository;
  private final RemissionGuideItemRepository itemRepository;
  private final RemissionGuideDriverRepository driverRepository;
  private final GoogleDriveService googleDriveService;
  private final ConfigurationService configurationService;

  @Value("${drive.folder-id.guias}")
  private String guiasFolderId;

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @Override
  @Transactional
  public void generatePdf(Long guideId) {
    try {
      RemissionGuide guide = guideRepository.findByIdAndDeletedAtIsNull(guideId)
          .orElseThrow(() -> new ResourceNotFoundException("Guía no encontrada"));

      List<RemissionGuideItem> items =
          itemRepository.findByRemissionGuideIdAndDeletedAtIsNull(guideId);

      if (items.isEmpty()) {
        throw new BusinessValidationException("La guía no tiene ítems registrados");
      }

      if (guide.getClient() == null) {
        throw new BusinessValidationException(
            "La guía no tiene destinatario asignado. Edite la guía y asigne un destinatario antes de generar el PDF.");
      }

      List<RemissionGuideDriver> drivers =
          driverRepository.findByRemissionGuideIdAndDeletedAtIsNull(guideId);

      // Primer conductor (para TRANSPORTE_PRIVADO)
      RemissionGuideDriver firstDriver = drivers.isEmpty() ? null : drivers.get(0);

      Map<String, String> config = configurationService.getGroup("empresa_emisora");

      // Construir cadena QR
      String numDoc = guide.getClient().getDocumentNumber();
      String qrString = config.get("emprRuc") + " | 09 | "
          + guide.getSeries() + " | "
          + guide.getSequence() + " | "
          + guide.getIssueDate().format(DATE_FMT) + " | 6 | "
          + numDoc;

      // Determinar si el destinatario es RUC (11 dígitos) o DNI (8 dígitos)
      String rucCliente = (numDoc != null && numDoc.length() == 11) ? numDoc : null;
      String dniCliente = (numDoc != null && numDoc.length() == 8) ? numDoc : null;

      // Una fila por ítem en el datasource
      List<Map<String, Object>> dataList = new ArrayList<>();

      for (RemissionGuideItem item : items) {

        Map<String, Object> row = new HashMap<>();

        // ====== EMPRESA ======
        row.put("empr_ruc", config.get("emprRuc"));
        row.put("empr_nombre_comercial", config.get("emprNombreComercial"));
        row.put("empr_razon_social", config.get("emprRazonSocial"));
        row.put("empr_direccion_fiscal", config.get("emprDireccionFiscal"));
        row.put("empr_telefono", config.get("emprTelefono"));
        row.put("empr_pagina_web", config.get("emprPaginaWeb"));
        row.put("empr_numero_autorizacion", "034-005-0005315");
        row.put("empr_imagen", null);
        row.put("empr_pdf_marca_agua", null);

        // ====== COMPROBANTE ======
        row.put("tico_descripcion", "GUÍA DE REMISIÓN ELECTRÓNICA");
        row.put("comp_numero_comprobante",
            guide.getSeries() + " - " + guide.getSequence());
        row.put("comp_fecha_traslado", Date.valueOf(guide.getTransferDate()));
        row.put("fecha_hora", Timestamp.valueOf(guide.getIssueDate()));
        row.put("fecha_traslado", Date.valueOf(guide.getTransferDate()));

        // ====== DESTINATARIO ======
        String clientName = guide.getClient().getBusinessName() != null
            ? guide.getClient().getBusinessName()
            : ((guide.getClient().getFirstName() != null ? guide.getClient().getFirstName() : "")
                + " " + (guide.getClient().getLastName() != null ? guide.getClient().getLastName() : "")).trim();
        row.put("nombre_representante", clientName);
        row.put("razonsocial", clientName);
        row.put("ruccliente", rucCliente);
        row.put("dni_cliente", dniCliente);

        // ====== PUNTOS DE TRASLADO ======
        row.put("direccionpartida", guide.getOriginAddress());
        row.put("direccionllegada", guide.getDestinationAddress());
        row.put("dist_partida", null);
        row.put("prov_partida", null);
        row.put("dpto_partida", null);
        row.put("dist_llegada", null);
        row.put("prov_llegada", null);
        row.put("dpto_llegada", null);
        row.put("direccion_referencia", null);
        row.put("dist_referencia", null);
        row.put("prov_referencia", null);
        row.put("dpto_referencia", null);

        // ====== TRANSPORTE ======
        row.put("tipo_transporte", guide.getTransportMode());
        row.put("num_bultos", guide.getPackageCount());
        row.put("peso", guide.getGrossWeight().doubleValue());
        row.put("tipo_guia", guide.getTransferReason());
        row.put("tipo_guia_descripcion",
            guide.getTransferReasonDescription() != null
                ? guide.getTransferReasonDescription()
                : guide.getTransferReason());

        // TRANSPORTE_PUBLICO
        String carrierName = (guide.getCarrier() != null) ? guide.getCarrier().getBusinessName() : null;
        String carrierDocNumber = (guide.getCarrier() != null) ? guide.getCarrier().getDocNumber() : null;
        row.put("transporte", carrierName);
        row.put("ructrasnporte",
            "TRANSPORTE_PUBLICO".equals(guide.getTransportMode())
                ? carrierDocNumber
                : (firstDriver != null && firstDriver.getDriver() != null
                    ? firstDriver.getDriver().getDocNumber() : null));

        // TRANSPORTE_PRIVADO
        row.put("vehiculo", null);
        row.put("placa", firstDriver != null ? firstDriver.getVehiclePlate() : null);
        row.put("chofer", firstDriver != null && firstDriver.getDriver() != null
            ? firstDriver.getDriver().getFirstName() + " " + firstDriver.getDriver().getLastName()
            : null);
        row.put("licencia", firstDriver != null && firstDriver.getDriver() != null
            ? firstDriver.getDriver().getLicenseNumber() : null);
        row.put("inscripcion", null);

        // ====== REFERENCIA / VARIOS ======
        row.put("comprobante", guide.getObservations());
        row.put("costominimo", 0.0);
        row.put("desde_venta", null);
        row.put("con_venta", null);
        row.put("flete", java.math.BigDecimal.ZERO);
        row.put("imprevistos", java.math.BigDecimal.ZERO);

        // ====== QR ======
        row.put("comp_cadena_qr", qrString);

        // ====== ÍTEM ======
        String sku = (item.getProduct() != null && item.getProduct().getSku() != null)
            ? item.getProduct().getSku()
            : "";
        row.put("codigo_producto", sku);
        row.put("codigo_prod_proveedor", sku);
        row.put("descripcion", item.getDescription());
        row.put("cantidad", item.getQuantity().doubleValue());
        row.put("abreviatura", item.getUnitMeasureSunat());
        row.put("precio_compra", 0.0);
        row.put("monto", 0.0);
        row.put("observaciones", "");
        row.put("mostrar_codigo_proveedor", "NO");
        row.put("mostrar_observacion_item", "NO");

        dataList.add(row);
      }

      // Compilar Jasper
      InputStream inputStream =
          getClass().getResourceAsStream("/jasper/GuiaRemisionElectronicaA4.jrxml");

      JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
      JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("urlImagen",
          Objects.requireNonNull(getClass().getResource("/img/logo.png")).toString());

      JasperPrint jasperPrint =
          JasperFillManager.fillReport(jasperReport, parameters, dataSource);

      // Nombre del archivo
      String fileName = config.get("emprRuc") + "-09-"
          + guide.getSeries() + "-"
          + guide.getSequence() + ".pdf";

      File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
      JasperExportManager.exportReportToPdfFile(jasperPrint, tempFile.getAbsolutePath());

      // Subir a Drive y guardar URL
      String fileId = googleDriveService.uploadPdf(tempFile, guiasFolderId);
      String driveUrl = "https://drive.google.com/file/d/" + fileId + "/view";

      guide.setPdfUrl(driveUrl);
      guideRepository.save(guide);

      tempFile.delete();

    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessValidationException(
          "Error al generar el PDF de la guía: " + e.getMessage());
    }
  }
}
