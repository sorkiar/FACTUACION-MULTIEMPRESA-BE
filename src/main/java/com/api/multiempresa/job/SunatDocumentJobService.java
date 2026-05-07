package com.api.multiempresa.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.Normalizer;
import com.api.multiempresa.dto.entity.CreditDebitNote;
import com.api.multiempresa.dto.entity.CreditDebitNoteItem;
import com.api.multiempresa.dto.entity.DetractionCode;
import com.api.multiempresa.dto.entity.Document;
import com.api.multiempresa.dto.entity.ExchangeRate;
import com.api.multiempresa.dto.entity.RemissionGuide;
import com.api.multiempresa.dto.entity.RemissionGuideDriver;
import com.api.multiempresa.dto.entity.RemissionGuideItem;
import com.api.multiempresa.dto.entity.Sale;
import com.api.multiempresa.dto.entity.SaleInstallment;
import com.api.multiempresa.dto.entity.SaleItem;
import com.api.multiempresa.dto.entity.SunatRequestLog;
import com.api.multiempresa.dto.external.ClientSendRequest;
import com.api.multiempresa.dto.external.CompanySendRequest;
import com.api.multiempresa.dto.external.DetraccionSendRequest;
import com.api.multiempresa.dto.external.DocumentSendRequest;
import com.api.multiempresa.dto.external.FacturacionResponse;
import com.api.multiempresa.dto.external.GuiaSendRequest;
import com.api.multiempresa.dto.external.GuiaTransporteSendRequest;
import com.api.multiempresa.dto.external.GuiaValidRequest;
import com.api.multiempresa.dto.external.ItemCuotaSendRequest;
import com.api.multiempresa.dto.external.ItemSendRequest;
import com.api.multiempresa.dto.external.NotaSendRequest;
import com.api.multiempresa.dto.external.SunatSendRequest;
import com.api.multiempresa.dto.external.TipoDetraccionSendRequest;
import com.api.multiempresa.dto.external.UbigeoSendRequest;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CreditDebitNoteItemRepository;
import com.api.multiempresa.repository.CreditDebitNoteRepository;
import com.api.multiempresa.repository.DetractionCodeRepository;
import com.api.multiempresa.repository.DocumentRepository;
import com.api.multiempresa.repository.ExchangeRateRepository;
import com.api.multiempresa.repository.RemissionGuideDriverRepository;
import com.api.multiempresa.repository.RemissionGuideItemRepository;
import com.api.multiempresa.repository.RemissionGuideRepository;
import com.api.multiempresa.repository.SaleInstallmentRepository;
import com.api.multiempresa.repository.SaleItemRepository;
import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.repository.SunatRequestLogRepository;
import com.api.multiempresa.service.GoogleDriveService;
import com.api.multiempresa.service.SunatSendConfigService;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SunatDocumentJobService {

  private final DocumentRepository documentRepository;
  private final SaleItemRepository saleItemRepository;
  private final SaleInstallmentRepository saleInstallmentRepository;
  private final CreditDebitNoteRepository creditDebitNoteRepository;
  private final CreditDebitNoteItemRepository creditDebitNoteItemRepository;
  private final RemissionGuideRepository remissionGuideRepository;
  private final RemissionGuideItemRepository remissionGuideItemRepository;
  private final RemissionGuideDriverRepository remissionGuideDriverRepository;
  private final DetractionCodeRepository detractionCodeRepository;
  private final ExchangeRateRepository exchangeRateRepository;
  private final SunatRequestLogRepository sunatRequestLogRepository;
  private final GoogleDriveService googleDriveService;
  private final SunatSendConfigService sunatSendConfigService;

  /** Registra el instante de la última ejecución del job por tipo de comprobante. */
  private final Map<String, LocalDateTime> lastRun = new ConcurrentHashMap<>();

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${sunat.url}")
  private String sunatUrl;

  @Value("${sunat.url-guia}")
  private String sunatGuiaUrl;

  @Value("${sunat.url-guia-valid}")
  private String sunatGuiaValidUrl;

  @Value("${drive.folder-id.boletas}")
  private String boletasFolderId;

  @Value("${drive.folder-id.facturas}")
  private String facturasFolderId;

  @Value("${drive.folder-id.notas}")
  private String notasFolderId;

  @Value("${drive.folder-id.guias}")
  private String guiasFolderId;


  /**
   * Tick base: se ejecuta cada minuto y procesa cada tipo de comprobante
   * según su modo de envío y el intervalo configurado en BD.
   *
   * <ul>
   *   <li>ONLINE  → el job no procesa ese tipo (el envío se realiza al instante en la creación).</li>
   *   <li>OFFLINE → el job procesa los comprobantes PENDIENTE cuando ha transcurrido el
   *                 intervalo configurado desde la última ejecución.</li>
   * </ul>
   */
  @Scheduled(fixedRate = 60000)
  public void scheduledTick() {
    processIfApplicable("factura", () -> sendPendingDocsByType("01"));
    processIfApplicable("boleta", () -> sendPendingDocsByType("03"));
    processIfApplicable("nota_credito", () -> sendPendingNotesByType("07"));
    processIfApplicable("nota_debito", () -> sendPendingNotesByType("08"));
    processIfApplicable("guia_remision", this::sendPendingRemissionGuides);
    checkPendingGuideTickets();
  }

  private void processIfApplicable(String docTypeKey, Runnable processor) {
    if (sunatSendConfigService.isOnlineMode(docTypeKey)) {
      return; // ONLINE: el envío se hace al instante en la creación
    }
    int intervalMinutes = sunatSendConfigService.getIntervalMinutes(docTypeKey);
    LocalDateTime last = lastRun.get(docTypeKey);
    if (last != null && ChronoUnit.MINUTES.between(last, LocalDateTime.now()) < intervalMinutes) {
      return; // Intervalo aún no transcurrido
    }
    lastRun.put(docTypeKey, LocalDateTime.now());
    processor.run();
  }

  private void sendPendingDocsByType(String sunatCode) {
    log.info("Iniciando proceso de envío a SUNAT (tipo {})...", sunatCode);

    List<Document> pendientes =
        documentRepository.findByStatusAndDocumentTypeSunat_CodeAndDeletedAtIsNull(
            "PENDIENTE", sunatCode);

    log.info("Documentos tipo {} pendientes: {}", sunatCode, pendientes.size());

    for (Document doc : pendientes) {
      try {
        Sale sale = doc.getSale();
        List<SaleItem> items =
            saleItemRepository.findBySaleIdAndDeletedAtIsNull(sale.getId());

        SunatSendRequest request = buildRequest(doc, sale, items);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SunatSendRequest> entity = new HttpEntity<>(request, headers);

        String requestJson = serializeJson(request);
        log.info("====== JSON ENVIADO A SUNAT ======\n{}", requestJson);

        LocalDateTime sentAt = LocalDateTime.now();
        ResponseEntity<FacturacionResponse> response =
            restTemplate.exchange(sunatUrl, HttpMethod.POST, entity, FacturacionResponse.class);

        processResponse(doc, response);
        saveLog("job-system", doc.getDocumentTypeSunat().getCode(),
            doc.getSeries(), doc.getSequence(),
            requestJson, serializeJson(response.getBody()),
            response.getStatusCode().value(), response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null && response.getBody().isSuccess(), sentAt);

      } catch (Exception e) {
        doc.setStatus("ERROR");
        doc.setSunatMessage("Error enviando: " + e.getMessage());
        log.error("Error enviando documento {}", doc.getId(), e);
      }

      doc.setUpdatedBy("job-system");
      documentRepository.save(doc);
    }

    log.info("Proceso SUNAT tipo {} finalizado.", sunatCode);
  }

  private void sendPendingNotesByType(String sunatCode) {
    log.info("Iniciando proceso de notas tipo {}...", sunatCode);

    List<CreditDebitNote> pendientes =
        creditDebitNoteRepository
            .findByStatusAndDocumentTypeSunat_CodeAndDeletedAtIsNull("PENDIENTE", sunatCode);

    log.info("Notas tipo {} pendientes: {}", sunatCode, pendientes.size());

    for (CreditDebitNote note : pendientes) {
      try {
        List<CreditDebitNoteItem> items =
            creditDebitNoteItemRepository
                .findByCreditDebitNoteIdAndDeletedAtIsNull(note.getId());

        SunatSendRequest request = buildNoteRequest(note, items);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SunatSendRequest> entity = new HttpEntity<>(request, headers);

        String requestJson = serializeJson(request);
        log.info("====== JSON NOTA ENVIADO A SUNAT ======\n{}", requestJson);

        LocalDateTime sentAt = LocalDateTime.now();
        ResponseEntity<FacturacionResponse> response =
            restTemplate.exchange(sunatUrl, HttpMethod.POST, entity, FacturacionResponse.class);

        processNoteResponse(note, response);
        saveLog("job-system", note.getDocumentTypeSunat().getCode(),
            note.getSeries(), note.getSequence(),
            requestJson, serializeJson(response.getBody()),
            response.getStatusCode().value(), response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null && response.getBody().isSuccess(), sentAt);

      } catch (Exception e) {
        note.setStatus("ERROR");
        note.setSunatMessage("Error enviando: " + e.getMessage());
        log.error("Error enviando nota {}", note.getId(), e);
      }

      note.setUpdatedBy("job-system");
      creditDebitNoteRepository.save(note);
    }

    log.info("Proceso notas tipo {} finalizado.", sunatCode);
  }

  // =====================================================
  // NOTAS DE CRÉDITO Y DÉBITO
  // =====================================================

  private SunatSendRequest buildNoteRequest(
      CreditDebitNote note,
      List<CreditDebitNoteItem> items) {

    Company company = note.getSale().getCompany();
    CompanySendRequest empresa = buildCompanySendRequest(company);

    // CLIENTE (del documento original)
    Sale sale = note.getSale();
    Document originalDoc = note.getOriginalDocument();
    String origDocTypeCode = originalDoc.getDocumentTypeSunat().getCode();

    ClientSendRequest cliente = new ClientSendRequest();
    cliente.setClieNumeroDocumento(sale.getClient().getDocumentNumber());
    cliente.setClieDireccion(sale.getClientAddress());

    if ("01".equals(origDocTypeCode)) {
      cliente.setClieRazonSocial(sale.getClient().getBusinessName());
      cliente.setTipoDocumentoIdentidad("RUC");
    } else {
      cliente.setClieRazonSocial(
          sale.getClient().getFirstName() + " " + sale.getClient().getLastName());
      cliente.setTipoDocumentoIdentidad("DNI");
    }

    // ITEMS
    List<ItemSendRequest> itemDtos = new ArrayList<>();
    for (CreditDebitNoteItem item : items) {

      BigDecimal cantidad = item.getQuantity();

      // SUNAT regla 3271: LineExtensionAmount = CreditedQuantity × Price/PriceAmount
      // → Price/PriceAmount debe ser el valor unitario NETO (subtotalAmount / cantidad)
      //   para que qty × valorUnitario == LineExtensionAmount (subtotalAmount).
      // El descuento ya está en subtotalAmount; no se envía AllowanceCharge separado.
      // Precio con IGV se deriva de totalAmount / cantidad (IGV calculado desde subtotal).
      BigDecimal netValorUnitario = cantidad.compareTo(BigDecimal.ZERO) != 0
          ? item.getSubtotalAmount().divide(cantidad, 6, RoundingMode.HALF_UP)
          : item.getUnitPrice();
      BigDecimal netPrecioConIgv = cantidad.compareTo(BigDecimal.ZERO) != 0
          ? item.getTotalAmount().divide(cantidad, 6, RoundingMode.HALF_UP)
          : item.getUnitPrice().multiply(new BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP);

      ItemSendRequest dto = new ItemSendRequest();
      String unidad = "NIU";
      if (item.getUnitMeasure() != null && item.getUnitMeasure().getCodeSunat() != null) {
        unidad = item.getUnitMeasure().getCodeSunat();
      }
      dto.setItcoUnidadMedida(unidad);
      dto.setItcoDescripcion(item.getDescription());
      dto.setItcoCantidad(cantidad);
      dto.setItcoValorUnitario(netValorUnitario);
      dto.setItcoPrecioUnitario(netPrecioConIgv);
      dto.setItcoDescuentoAfecta(BigDecimal.ZERO);
      dto.setItcoSubTotal(item.getSubtotalAmount());
      dto.setItcoIgv(item.getTaxAmount());
      dto.setItcoTotal(item.getTotalAmount());
      dto.setTipoAfectacionIgv("GRAVADO");
      itemDtos.add(dto);
    }

    // NOTA (referencia al documento original)
    String tipoNotaEnumName = resolveNoteTypeEnumName(note.getCreditDebitNoteType().getCode());
    String tipoDocAfectaEnumName = "01".equals(origDocTypeCode) ? "FACTURA" : "BOLETA";

    NotaSendRequest nota = new NotaSendRequest();
    nota.setNotaSerieModifica(originalDoc.getSeries());
    nota.setNotaNumeroModifica(Integer.parseInt(originalDoc.getSequence()));
    nota.setNotaFechaModifica(originalDoc.getIssueDate().toLocalDate().toString());
    nota.setTipoNotaCreditoDebito(tipoNotaEnumName);
    nota.setTipoDocumentoAfecta(tipoDocAfectaEnumName);

    // COMPROBANTE
    String tipoDocumento = "07".equals(note.getDocumentTypeSunat().getCode())
        ? "NOTA_CREDITO" : "NOTA_DEBITO";

    DocumentSendRequest comprobante = new DocumentSendRequest();
    comprobante.setCompSerie(note.getSeries());
    comprobante.setCompNumero(Integer.parseInt(note.getSequence()));
    comprobante.setCompFechaEmision(note.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    comprobante.setCompPorcentajeIgv(new BigDecimal("18"));
    comprobante.setCompCondicionPago("CONTADO");
    comprobante.setCompMedioPago("EFECTIVO");
    comprobante.setMoneda(note.getCurrencyCode());
    comprobante.setTipoDocumento(tipoDocumento);
    comprobante.setTipoOperacion("VENTA_INTERNA");
    comprobante.setCliente(cliente);
    comprobante.setLsItemComprobante(itemDtos);
    comprobante.setNota(nota);

    SunatSendRequest request = new SunatSendRequest();
    request.setEmpresa(empresa);
    request.setComprobante(comprobante);

    return request;
  }

  private void processNoteResponse(CreditDebitNote note,
                                   ResponseEntity<FacturacionResponse> response) {

    FacturacionResponse body = response.getBody();

    if (response.getStatusCode().is2xxSuccessful() && body != null && body.isSuccess()) {

      var data = body.getData();

      if ("ACEPTADO".equalsIgnoreCase(data.getDeclarationResultType())) {
        note.setStatus("ACEPTADO");
      } else {
        note.setStatus("RECHAZADO");
      }

      note.setSunatResponseCode(data.getResponseCode());
      note.setSunatMessage(data.getMessage());
      note.setHashCode(data.getHashCode());
      note.setQrCode(data.getQrCode());
      note.setXmlBase64(data.getXmlBase64());
      note.setCdrBase64(data.getCdrBase64());

      if ("ACEPTADO".equals(note.getStatus())
          && data.getXmlBase64() != null
          && data.getCdrBase64() != null) {
        String[] urls = uploadXmlAndCdr(
            note.getSale().getCompany().getRuc(),
            note.getDocumentTypeSunat().getCode(),
            note.getSeries(), note.getSequence(),
            data.getXmlBase64(), data.getCdrBase64(), notasFolderId);
        note.setXmlUrl(urls[0]);
        note.setCdrUrl(urls[1]);
      }

    } else {
      note.setStatus("ERROR");
      note.setSunatMessage(body != null ? body.getMessage() : "HTTP Error: " + response.getStatusCode());
    }
  }

  // =====================================================
  // ENVÍO INMEDIATO (resend manual)
  // =====================================================

  @org.springframework.transaction.annotation.Transactional
  public void sendDocumentNow(Document doc) {
    try {
      Sale sale = doc.getSale();
      List<SaleItem> items =
          saleItemRepository.findBySaleIdAndDeletedAtIsNull(sale.getId());
      SunatSendRequest request = buildRequest(doc, sale, items);

      String requestJson = serializeJson(request);
      log.info("====== JSON ENVIADO A SUNAT ======\n{}", requestJson);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      LocalDateTime sentAt = LocalDateTime.now();
      ResponseEntity<FacturacionResponse> response = restTemplate.exchange(
          sunatUrl, HttpMethod.POST, new HttpEntity<>(request, headers),
          FacturacionResponse.class);
      processResponse(doc, response);
      saveLog("manual-resend", doc.getDocumentTypeSunat().getCode(),
          doc.getSeries(), doc.getSequence(),
          requestJson, serializeJson(response.getBody()),
          response.getStatusCode().value(), response.getStatusCode().is2xxSuccessful()
              && response.getBody() != null && response.getBody().isSuccess(), sentAt);
    } catch (Exception e) {
      doc.setStatus("ERROR");
      doc.setSunatMessage("Error enviando: " + e.getMessage());
      log.error("Error enviando documento {} de forma manual", doc.getId(), e);
    }
    doc.setUpdatedBy("manual-resend");
    documentRepository.save(doc);
  }

  @org.springframework.transaction.annotation.Transactional
  public void sendCreditDebitNoteNow(CreditDebitNote note) {
    try {
      List<CreditDebitNoteItem> items =
          creditDebitNoteItemRepository.findByCreditDebitNoteIdAndDeletedAtIsNull(note.getId());
      SunatSendRequest request = buildNoteRequest(note, items);

      String requestJson = serializeJson(request);
      log.info("====== JSON NOTA ENVIADO A SUNAT ======\n{}", requestJson);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      LocalDateTime sentAt = LocalDateTime.now();
      ResponseEntity<FacturacionResponse> response = restTemplate.exchange(
          sunatUrl, HttpMethod.POST, new HttpEntity<>(request, headers),
          FacturacionResponse.class);
      processNoteResponse(note, response);
      saveLog("manual-resend", note.getDocumentTypeSunat().getCode(),
          note.getSeries(), note.getSequence(),
          requestJson, serializeJson(response.getBody()),
          response.getStatusCode().value(), response.getStatusCode().is2xxSuccessful()
              && response.getBody() != null && response.getBody().isSuccess(), sentAt);
    } catch (Exception e) {
      note.setStatus("ERROR");
      note.setSunatMessage("Error enviando: " + e.getMessage());
      log.error("Error enviando nota {} de forma manual", note.getId(), e);
    }
    note.setUpdatedBy("manual-resend");
    creditDebitNoteRepository.save(note);
  }

  @org.springframework.transaction.annotation.Transactional
  public void sendRemissionGuideNow(RemissionGuide guide) {
    try {
      List<RemissionGuideItem> items =
          remissionGuideItemRepository.findByRemissionGuideIdAndDeletedAtIsNull(guide.getId());
      List<RemissionGuideDriver> drivers =
          remissionGuideDriverRepository.findByRemissionGuideIdAndDeletedAtIsNull(guide.getId());
      SunatSendRequest request = buildGuideRequest(guide, items, drivers);

      String requestJson = serializeJson(request);
      log.info("====== JSON GUÍA ENVIADO A SUNAT ======\n{}", requestJson);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      LocalDateTime sentAt = LocalDateTime.now();
      ResponseEntity<FacturacionResponse> response = restTemplate.exchange(
          sunatGuiaUrl, HttpMethod.POST, new HttpEntity<>(request, headers),
          FacturacionResponse.class);
      processGuideResponse(guide, response);
      saveLog("manual-resend", "09",
          guide.getSeries(), guide.getSequence(),
          requestJson, serializeJson(response.getBody()),
          response.getStatusCode().value(), response.getStatusCode().is2xxSuccessful()
              && response.getBody() != null && response.getBody().isSuccess(), sentAt);
    } catch (Exception e) {
      guide.setStatus("ERROR");
      guide.setSunatMessage("Error enviando: " + e.getMessage());
      log.error("Error enviando guía {} de forma manual", guide.getId(), e);
    }
    guide.setUpdatedBy("manual-resend");
    remissionGuideRepository.save(guide);
  }

  // =====================================================
  // GUÍAS DE REMISIÓN
  // =====================================================

  private void sendPendingRemissionGuides() {

    log.info("Iniciando proceso de guías de remisión...");

    List<RemissionGuide> pendientes =
        remissionGuideRepository.findByStatusAndDeletedAtIsNull("PENDIENTE");

    log.info("Guías pendientes encontradas: {}", pendientes.size());

    for (RemissionGuide guide : pendientes) {

      try {

        List<RemissionGuideItem> items =
            remissionGuideItemRepository
                .findByRemissionGuideIdAndDeletedAtIsNull(guide.getId());

        List<RemissionGuideDriver> drivers =
            remissionGuideDriverRepository
                .findByRemissionGuideIdAndDeletedAtIsNull(guide.getId());

        SunatSendRequest request = buildGuideRequest(guide, items, drivers);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SunatSendRequest> entity = new HttpEntity<>(request, headers);

        String requestJson = serializeJson(request);
        log.info("====== JSON GUÍA ENVIADO A SUNAT ======\n{}", requestJson);

        LocalDateTime sentAt = LocalDateTime.now();
        ResponseEntity<FacturacionResponse> response =
            restTemplate.exchange(sunatGuiaUrl, HttpMethod.POST, entity, FacturacionResponse.class);

        processGuideResponse(guide, response);
        saveLog("job-system", "09",
            guide.getSeries(), guide.getSequence(),
            requestJson, serializeJson(response.getBody()),
            response.getStatusCode().value(), response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null && response.getBody().isSuccess(), sentAt);

      } catch (Exception e) {
        guide.setStatus("ERROR");
        guide.setSunatMessage("Error enviando: " + e.getMessage());
        log.error("Error enviando guía {}", guide.getId(), e);
      }

      guide.setUpdatedBy("job-system");
      remissionGuideRepository.save(guide);
    }

    log.info("Proceso guías SUNAT finalizado.");
  }

  private SunatSendRequest buildGuideRequest(
      RemissionGuide guide,
      List<RemissionGuideItem> items,
      List<RemissionGuideDriver> drivers) {

    Company company = guide.getCompany();
    CompanySendRequest empresa = buildCompanySendRequestWithGuia(company);

    // Destinatario (como cliente del comprobante)
    ClientSendRequest recipient = new ClientSendRequest();
    recipient.setClieNumeroDocumento(guide.getClient().getDocumentNumber());
    String guideClientName;
    String bName = guide.getClient().getBusinessName();
    if (bName != null && !bName.isBlank()) {
      guideClientName = bName;
    } else {
      guideClientName = (guide.getClient().getFirstName() != null ? guide.getClient().getFirstName() : "")
          + " " + (guide.getClient().getLastName() != null ? guide.getClient().getLastName() : "");
    }
    recipient.setClieRazonSocial(guideClientName.trim());
    recipient.setClieDireccion(guide.getClientAddress());
    recipient.setTipoDocumentoIdentidad(
        resolveDocIdentidad(guide.getClient().getDocumentType().getName()));

    // ITEMS (bienes a transportar)
    List<ItemSendRequest> itemDtos = new ArrayList<>();
    for (RemissionGuideItem item : items) {
      BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
      BigDecimal subtotal = item.getSubtotalAmount() != null ? item.getSubtotalAmount() : BigDecimal.ZERO;
      BigDecimal igv = item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO;
      BigDecimal total = item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO;
      BigDecimal valorUnitario = unitPrice.compareTo(BigDecimal.ZERO) > 0
          ? unitPrice.divide(new BigDecimal("1.18"), 6, RoundingMode.HALF_UP)
          : BigDecimal.ZERO;

      ItemSendRequest dto = new ItemSendRequest();
      dto.setItcoUnidadMedida(item.getUnitMeasureSunat());
      dto.setItcoDescripcion(item.getDescription());
      dto.setItcoCantidad(item.getQuantity());
      dto.setItcoValorUnitario(valorUnitario); // 6 dec — evita desfase con itcoSubTotal al multiplicar por cantidad
      dto.setItcoPrecioUnitario(unitPrice.setScale(2, RoundingMode.HALF_UP));
      dto.setItcoSubTotal(subtotal);
      dto.setItcoIgv(igv);
      dto.setItcoTotal(total);
      dto.setTipoAfectacionIgv("GRAVADO");
      itemDtos.add(dto);
    }

    // GUÍA
    GuiaSendRequest guia = new GuiaSendRequest();
    guia.setGuiaPesoBruto(guide.getGrossWeight());
    guia.setGuiaNumeroBultos(guide.getPackageCount());
    guia.setGuiaUnidadMedidaPeso(guide.getWeightUnit());
    guia.setGuiaFechaTraslado(guide.getTransferDate().toString());
    guia.setGuiaPuntoPartidaDireccion(guide.getOriginAddress());
    guia.setGuiaUbigeoPartida(guide.getOriginUbigeo());
    guia.setGuiaCodigoLocalPartida(guide.getOriginLocalCode());
    guia.setGuiaPuntoLlegadaDireccion(guide.getDestinationAddress());
    guia.setGuiaUbigeoLlegada(guide.getDestinationUbigeo());
    guia.setGuiaCodigoLocalLlegada(guide.getDestinationLocalCode());
    guia.setMotivoTraslado(guide.getTransferReason());
    guia.setGuiaMotivoTrasladoDescripcion(guide.getTransferReasonDescription());
    guia.setTipoTransporte(guide.getTransportMode());
    guia.setGuiaTrasladoVehiculoMenores(guide.getMinorVehicleTransfer());

    if ("TRANSPORTE_PUBLICO".equals(guide.getTransportMode()) && guide.getCarrier() != null) {
      ClientSendRequest transportista = new ClientSendRequest();
      transportista.setClieNumeroDocumento(guide.getCarrier().getDocNumber());
      transportista.setClieRazonSocial(guide.getCarrier().getBusinessName());
      transportista.setTipoDocumentoIdentidad(
          resolveDocIdentidad(guide.getCarrier().getDocType()));
      guia.setTransportista(transportista);
    }

    if ("TRANSPORTE_PRIVADO".equals(guide.getTransportMode()) && !drivers.isEmpty()) {
      List<GuiaTransporteSendRequest> transporteDtos = new ArrayList<>();
      for (RemissionGuideDriver rgDriver : drivers) {
        if (rgDriver.getDriver() == null) continue;
        GuiaTransporteSendRequest dto = new GuiaTransporteSendRequest();
        dto.setGutrConductorTipoDocumento(resolveDocIdentidadCat06(rgDriver.getDriver().getDocType()));
        dto.setGutrConductorNumeroDocumento(rgDriver.getDriver().getDocNumber());
        dto.setGutrConductorNombres(rgDriver.getDriver().getFirstName());
        dto.setGutrConductorApellidos(rgDriver.getDriver().getLastName());
        dto.setGutrConductorNumeroLicencia(rgDriver.getDriver().getLicenseNumber());
        dto.setGutrVehiculoPlaca(
            rgDriver.getVehiclePlate() != null ? rgDriver.getVehiclePlate() : "");
        transporteDtos.add(dto);
      }
      guia.setLsGuiaTransporte(transporteDtos);
    }

    // COMPROBANTE
    DocumentSendRequest comprobante = new DocumentSendRequest();
    comprobante.setCompSerie(guide.getSeries());
    comprobante.setCompNumero(Integer.parseInt(guide.getSequence()));
    comprobante.setCompFechaEmision(guide.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    comprobante.setCompHoraEmision(guide.getIssueDate().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    comprobante.setCompPorcentajeIgv(new BigDecimal("18"));
    comprobante.setCompCondicionPago("CONTADO");
    comprobante.setCompMedioPago("EFECTIVO");
    comprobante.setMoneda("PEN");
    comprobante.setTipoDocumento("GUIA_REMISION_REMITENTE");
    comprobante.setTipoOperacion("VENTA_INTERNA");
    comprobante.setCliente(recipient);
    comprobante.setLsItemComprobante(itemDtos);
    comprobante.setGuia(guia);

    SunatSendRequest request = new SunatSendRequest();
    request.setEmpresa(empresa);
    request.setComprobante(comprobante);

    return request;
  }

  private void processGuideResponse(RemissionGuide guide,
                                    ResponseEntity<FacturacionResponse> response) {

    FacturacionResponse body = response.getBody();

    if (response.getStatusCode().is2xxSuccessful() && body != null && body.isSuccess()) {

      var data = body.getData();
      guide.setSunatResponseCode(data.getResponseCode());
      guide.setSunatMessage(data.getMessage());

      if ("ACEPTADO".equalsIgnoreCase(data.getDeclarationResultType())) {
        // CDR disponible; XML ya fue guardado en la etapa EN_PROCESO
        guide.setStatus("ACEPTADO");
        guide.setCdrBase64(data.getCdrBase64());
        String xmlToUpload = guide.getXmlBase64();
        if (xmlToUpload != null && data.getCdrBase64() != null) {
          String[] urls = uploadXmlAndCdr(
              guide.getCompany().getRuc(),
              "09", guide.getSeries(), guide.getSequence(),
              xmlToUpload, data.getCdrBase64(), guiasFolderId);
          guide.setXmlUrl(urls[0]);
          guide.setCdrUrl(urls[1]);
        }

      } else if ("EN_PROCESO".equalsIgnoreCase(data.getDeclarationResultType())) {
        // SUNAT recibió la guía y emitió un ticket; XML firmado ya disponible
        guide.setStatus("EN PROCESO");
        if (data.getTicket() != null && !data.getTicket().isEmpty()) {
          guide.setSunatTicket(data.getTicket());
        }
        if (data.getXmlBase64() != null) {
          guide.setXmlBase64(data.getXmlBase64());
        }
        if (data.getHashCode() != null && !data.getHashCode().isEmpty()) {
          guide.setHashCode(data.getHashCode());
        }

      } else {
        // EXCEPCION: responseCode=99 es rechazo formal de SUNAT; otros códigos son error técnico
        String errMsg = data.getMessage();
        if (data.getObservation() != null && !data.getObservation().isEmpty()) {
          errMsg = errMsg != null ? errMsg : data.getObservation();
        }
        guide.setSunatMessage(errMsg);
        guide.setStatus(Integer.valueOf(99).equals(data.getResponseCode()) ? "RECHAZADO" : "ERROR");
      }

    } else {
      guide.setStatus("ERROR");
      guide.setSunatMessage(body != null ? body.getMessage() : "HTTP Error: " + response.getStatusCode());
    }
  }

  /** Resuelve el nombre del tipo de documento de identidad para el facturador. */
  private String resolveDocIdentidad(String tipoDoc) {
    if (tipoDoc == null) return "RUC";
    // Normalizar: quitar tildes y pasar a mayúsculas para manejar nombres display de la BD
    String normalized = Normalizer.normalize(tipoDoc, Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
        .toUpperCase();
    return switch (normalized) {
      case "DNI", "1" -> "DNI";
      case "RUC", "6" -> "RUC";
      case "CE", "4", "CARNE DE EXTRANJERIA", "CARNET DE EXTRANJERIA",
          "CARNET EXTRANJERIA", "CARNE EXTRANJERIA" -> "CARNET_EXTRANJERIA";
      case "PASAPORTE", "7" -> "PASAPORTE";
      case "CEDULA DIPLOMATICA", "A" -> "CEDULA_DIPLOMATICA";
      case "SIN DOCUMENTO", "0" -> "SIN_DOCUMENTO";
      case "TARJETA ANDINA", "B" -> "TARJETA_ANDINA";
      default -> tipoDoc;
    };
  }

  // =====================================================
  // LOG HELPERS
  // =====================================================

  private String serializeJson(Object obj) {
    if (obj == null) return null;
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.findAndRegisterModules();
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (Exception e) {
      log.warn("No se pudo serializar objeto a JSON: {}", e.getMessage());
      return obj.toString();
    }
  }

  private void saveLog(String triggeredBy, String documentType, String series, String sequence,
                       String requestJson, String responseJson, int httpStatus,
                       boolean success, LocalDateTime sentAt) {
    try {
      SunatRequestLog entry = new SunatRequestLog();
      entry.setTriggeredBy(triggeredBy);
      entry.setDocumentType(documentType);
      entry.setSeries(series);
      entry.setSequence(sequence);
      entry.setRequestJson(requestJson);
      entry.setResponseJson(responseJson);
      entry.setHttpStatus(httpStatus);
      entry.setSuccess(success);
      entry.setSentAt(sentAt);
      sunatRequestLogRepository.save(entry);
    } catch (Exception e) {
      log.error("Error guardando SunatRequestLog: {}", e.getMessage(), e);
    }
  }

  /**
   * Resuelve el código del Catálogo 06 (SUNAT) para gutrConductorTipoDocumento.
   * Referencia: Tabla 2 - Código de tipo de documento de identidad (Catálogo 06).
   */
  private String resolveDocIdentidadCat06(String tipoDoc) {
    if (tipoDoc == null) return "1";
    return switch (tipoDoc.toUpperCase()) {
      case "DNI", "1" -> "1";
      case "CE", "4" -> "4";
      case "RUC", "6" -> "6";
      case "PASAPORTE", "7" -> "7";
      case "CDIP", "A" -> "A";
      case "B" -> "B";
      default -> tipoDoc;
    };
  }

  /**
   * Mapea el código de nota (C01, D02, etc.) al nombre del enum
   * Catalog09_10TipoNotaCreditoDebito del facturador.
   */
  private String resolveNoteTypeEnumName(String noteTypeCode) {
    return switch (noteTypeCode) {
      case "C01" -> "CREDITO_ANULACION_OPERACION";
      case "C02" -> "CREDITO_ANULACION_ERROR_RUC";
      case "C03" -> "CREDITO_CORRECCION_ERROR_DESCRIPCION";
      case "C04" -> "CREDITO_DESCUENTO_GLOBAL";
      case "C05" -> "CREDITO_DESCUENTO_POR_ITEM";
      case "C06" -> "CREDITO_DEVOLUCION_TOTAL";
      case "C07" -> "CREDITO_DEVOLUCION_POR_ITEM";
      case "C08" -> "CREDITO_BONIFICACION";
      case "C09" -> "CREDITO_DISMINUCION_VALOR";
      case "C10" -> "CREDITO_OTROS_CONCEPTOS";
      case "C11" -> "CREDITO_AJUSTES_OPERACIONES_EXPORTACION";
      case "C12" -> "CREDITO_AJUSTES_IVAP";
      case "C13" -> "CREDITO_AJUSTES_FECHA_MONTO_CREDITO";
      case "D01" -> "DEBITO_INTERESES_MORA";
      case "D02" -> "DEBITO_AUMENTO_VALOR";
      case "D03" -> "DEBITO_PENALIDADES";
      case "D11" -> "DEBITO_AJUSTES_OPERACIONES_EXPORTACION";
      case "D12" -> "DEBITO_AJUSTES_IVAP";
      default -> throw new IllegalArgumentException("Código de nota inválido: " + noteTypeCode);
    };
  }

  // =====================================================
  // BUILD REQUEST
  // =====================================================

  private SunatSendRequest buildRequest(
      Document doc,
      Sale sale,
      List<SaleItem> items) {

    Company company = sale.getCompany();

    // ==========================
    // EMPRESA
    // ==========================
    CompanySendRequest empresa = buildCompanySendRequest(company);

    // ==========================
    // CLIENTE
    // ==========================
    ClientSendRequest cliente = new ClientSendRequest();

    cliente.setClieNumeroDocumento(
        sale.getClient().getDocumentNumber());

    cliente.setClieDireccion(sale.getClientAddress());

    if ("01".equals(doc.getDocumentTypeSunat().getCode())) {
      cliente.setClieRazonSocial(
          sale.getClient().getBusinessName());
      cliente.setTipoDocumentoIdentidad(
          "01".equals(doc.getDocumentTypeSunat().getCode())
              ? "RUC"
              : "DNI"
      );
    } else {
      cliente.setClieRazonSocial(
          sale.getClient().getFirstName() + " "
              + sale.getClient().getLastName());
      cliente.setTipoDocumentoIdentidad(
          "01".equals(doc.getDocumentTypeSunat().getCode())
              ? "RUC"
              : "DNI"
      );
    }

    // ==========================
    // ITEMS
    // ==========================
    List<ItemSendRequest> itemDtos = new ArrayList<>();

    for (SaleItem item : items) {

      BigDecimal cantidad = item.getQuantity();

      // unitPrice es ahora la BASE sin IGV (nuevo modelo: precios excluyen IGV)
      BigDecimal valorUnitario = item.getUnitPrice();
      BigDecimal precioConIgv = item.getUnitPrice()
          .multiply(new BigDecimal("1.18"))
          .setScale(2, RoundingMode.HALF_UP);

      // Descuento en base: grossBase - subtotalAmount (0 si no hay descuento)
      // Garantiza: valorUnitario * cantidad - descuentoAfecta == itcoSubTotal
      BigDecimal grossTotal = cantidad.multiply(item.getUnitPrice())
          .setScale(2, RoundingMode.HALF_UP);
      BigDecimal descuentoAfecta = grossTotal
          .subtract(item.getSubtotalAmount())
          .setScale(2, RoundingMode.HALF_UP);

      ItemSendRequest dto = new ItemSendRequest();
      String unidad = "NIU";
      if (item.getUnitMeasure() != null &&
          item.getUnitMeasure().getCodeSunat() != null) {
        unidad = item.getUnitMeasure().getCodeSunat();
      }
      dto.setItcoUnidadMedida(unidad);
      dto.setItcoDescripcion(item.getDescription());
      dto.setItcoCantidad(cantidad);
      dto.setItcoValorUnitario(valorUnitario);
      dto.setItcoPrecioUnitario(precioConIgv.setScale(2, RoundingMode.HALF_UP));
      dto.setItcoDescuentoAfecta(descuentoAfecta);
      dto.setItcoSubTotal(item.getSubtotalAmount());
      dto.setItcoIgv(item.getTaxAmount());
      dto.setItcoTotal(item.getTotalAmount());
      dto.setTipoAfectacionIgv("GRAVADO");

      // Retención por ítem en moneda del comprobante (el facturador lo coloca en AllowanceCharge
      // con currencyID de la moneda del doc; SUNAT valida Amount = MultiplierFactor × BaseAmount)
      if (Boolean.TRUE.equals(sale.getHasRetention()) && sale.getRetentionRate() != null) {
        BigDecimal itemRetention = item.getTotalAmount()
            .multiply(sale.getRetentionRate())
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        dto.setItcoRetencion(itemRetention);
      }

      itemDtos.add(dto);
    }

    // ==========================
    // COMPROBANTE
    // ==========================
    String condicionPago = sale.getPaymentType() != null ? sale.getPaymentType() : "CONTADO";

    DocumentSendRequest comprobante = new DocumentSendRequest();
    comprobante.setCompSerie(doc.getSeries());
    comprobante.setCompNumero(
        Integer.parseInt(doc.getSequence()));
    comprobante.setCompFechaEmision(doc.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    comprobante.setCompPorcentajeIgv(
        new BigDecimal("18"));
    comprobante.setCompCondicionPago(condicionPago);
    comprobante.setCompMedioPago("EFECTIVO");
    comprobante.setMoneda(sale.getCurrencyCode());
    comprobante.setTipoDocumento(
        "01".equals(doc.getDocumentTypeSunat().getCode())
            ? "FACTURA"
            : "BOLETA"
    );
    comprobante.setTipoOperacion("VENTA_INTERNA");

    // Retención: agrega observación si aplica.
    // itcoRetencion por ítem ya se setea arriba; el facturador genera AllowanceCharge
    // código 02 (Catálogo 53) y calcula FormaPago/Credito = sum(cuotas) automáticamente.
    if (Boolean.TRUE.equals(sale.getHasRetention()) && sale.getRetentionRate() != null) {
      comprobante.setCompObservaciones(
          "OPERACION SUJETA A RETENCIÓN DEL "
              + sale.getRetentionRate().stripTrailingZeros().toPlainString()
              + "% - RG N° 037-2002/SUNAT"
      );
    }

    comprobante.setCliente(cliente);
    comprobante.setLsItemComprobante(itemDtos);

    // Cuotas: solo cuando la condición de pago es CREDITO
    if ("CREDITO".equals(condicionPago)) {
      List<SaleInstallment> installments =
          saleInstallmentRepository
              .findBySaleIdAndDeletedAtIsNullOrderByInstallmentNumberAsc(sale.getId());
      List<ItemCuotaSendRequest> cuotas = new ArrayList<>();
      for (SaleInstallment inst : installments) {
        ItemCuotaSendRequest cuota = new ItemCuotaSendRequest();
        cuota.setItcuCuota(inst.getInstallmentNumber());
        cuota.setItcuFecha(
            inst.getDueDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        cuota.setItcuMonto(inst.getAmount());
        cuotas.add(cuota);
      }
      comprobante.setLsItemCuota(cuotas);
    }

    // ==========================
    // DETRACCIÓN (solo facturas en PEN)
    // ==========================
    if (Boolean.TRUE.equals(sale.getHasDetraction())
        && sale.getDetractionCode() != null) {

      String detrAccount = company.getSunatDetractionAccount() != null
          ? company.getSunatDetractionAccount() : "";

      // detrMonto siempre en PEN:
      // - PEN: usar el valor almacenado
      // - USD: recalcular con el tipo de cambio venta de la fecha de emisión
      BigDecimal detrMontoPen;
      if ("USD".equals(sale.getCurrencyCode())) {
        LocalDate issueDate = doc.getIssueDate().toLocalDate();
        BigDecimal exchangeRateValue = exchangeRateRepository.findByDate(issueDate).stream()
            .filter(r -> "V".equals(r.getType()))
            .map(ExchangeRate::getValue)
            .findFirst()
            .orElse(BigDecimal.ONE);
        detrMontoPen = sale.getTotalAmount()
            .multiply(sale.getDetractionRate())
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
            .multiply(exchangeRateValue)
            .setScale(2, RoundingMode.HALF_UP);
      } else {
        detrMontoPen = sale.getDetractionAmount();
      }

      DetractionCode dc = detractionCodeRepository
          .findByCode(sale.getDetractionCode()).orElse(null);

      TipoDetraccionSendRequest tipo = new TipoDetraccionSendRequest();
      tipo.setTideCodigo(sale.getDetractionCode());
      tipo.setTideDescripcion(dc != null ? dc.getDescription() : "");
      tipo.setTideEstado(true);

      DetraccionSendRequest detr = new DetraccionSendRequest();
      detr.setDetrCuenta(detrAccount);
      detr.setDetrPorcentaje(sale.getDetractionRate());
      detr.setDetrMonto(detrMontoPen);
      detr.setTipoDetraccion(tipo);

      comprobante.setCompDetraccion(true);
      comprobante.setCompCuentaDetraccion(detrAccount);
      comprobante.setLsDetraccion(List.of(detr));
    }

    // ==========================
    // WRAPPER
    // ==========================
    SunatSendRequest request = new SunatSendRequest();
    request.setEmpresa(empresa);
    request.setComprobante(comprobante);

    return request;
  }

  // =====================================================
  // RESPONSE
  // =====================================================

  private void processResponse(Document doc,
                               ResponseEntity<FacturacionResponse> response) {

    FacturacionResponse body = response.getBody();

    if (response.getStatusCode().is2xxSuccessful()
        && body != null
        && body.isSuccess()) {

      var data = body.getData();

      if ("ACEPTADO".equalsIgnoreCase(
          data.getDeclarationResultType())) {
        doc.setStatus("ACEPTADO");
      } else {
        doc.setStatus("RECHAZADO");
      }

      doc.setSunatResponseCode(data.getResponseCode());
      doc.setSunatMessage(data.getMessage());
      doc.setHashCode(data.getHashCode());
      doc.setQrCode(data.getQrCode());
      doc.setXmlBase64(data.getXmlBase64());
      doc.setCdrBase64(data.getCdrBase64());

      if ("ACEPTADO".equals(doc.getStatus())
          && data.getXmlBase64() != null
          && data.getCdrBase64() != null) {
        String docFolderId = "01".equals(doc.getDocumentTypeSunat().getCode())
            ? facturasFolderId : boletasFolderId;
        String[] urls = uploadXmlAndCdr(
            doc.getSale().getCompany().getRuc(),
            doc.getDocumentTypeSunat().getCode(),
            doc.getSeries(), doc.getSequence(),
            data.getXmlBase64(), data.getCdrBase64(), docFolderId);
        doc.setXmlUrl(urls[0]);
        doc.setCdrUrl(urls[1]);
      }

    } else {
      doc.setStatus("ERROR");
      assert response.getBody() != null;
      doc.setSunatMessage(
          "HTTP Error: " + response.getBody().getMessage());
    }
  }

  // =====================================================
  // COMPANY SEND REQUEST BUILDERS
  // =====================================================

  private CompanySendRequest buildCompanySendRequest(Company company) {
    CompanySendRequest empresa = new CompanySendRequest();
    empresa.setEmprRuc(company.getRuc());
    empresa.setEmprRazonSocial(company.getBusinessName());
    empresa.setEmprDireccionFiscal(company.getAddress());
    empresa.setEmprCodigoEstablecimientoSunat(company.getSunatEstablishmentCode());
    empresa.setEmprLeyAmazonia(Boolean.TRUE.equals(company.getSunatAmazoniaLaw()));
    empresa.setEmprProduccion(Boolean.TRUE.equals(company.getSunatProduction()));
    empresa.setEmprCertificadoLLavePublica(company.getSunatCertificatePublicKey());
    empresa.setEmprCertificadoLLavePrivada(company.getSunatCertificatePrivateKey());
    empresa.setEmprUsuarioSecundario(company.getSunatSecondaryUser());
    empresa.setEmprClaveUsuarioSecundario(company.getSunatSecondaryUserPassword());

    UbigeoSendRequest ubigeo = new UbigeoSendRequest();
    ubigeo.setUbigUbigeo(company.getUbigeo());
    ubigeo.setUbigDepartamento(company.getUbigDepartment());
    ubigeo.setUbigProvincia(company.getUbigProvince());
    ubigeo.setUbigDistrito(company.getUbigDistrict());
    empresa.setUbigeo(ubigeo);

    return empresa;
  }

  private CompanySendRequest buildCompanySendRequestWithGuia(Company company) {
    CompanySendRequest empresa = buildCompanySendRequest(company);
    empresa.setEmprGuiaId(company.getSunatGuideId());
    empresa.setEmprGuiaClave(company.getSunatGuidePassword());
    return empresa;
  }

  private String[] uploadXmlAndCdr(String companyRuc, String typeCode, String series,
                                   String sequence, String xmlBase64, String cdrBase64,
                                   String folderId) {
    try {
      String xmlFileName = "XML-" + companyRuc + "-" + typeCode + "-"
          + series + "-" + sequence + ".xml";
      String cdrFileName = "CDR-" + companyRuc + "-" + typeCode + "-"
          + series + "-" + sequence + ".xml";

      File xmlFile = new File(
          System.getProperty("java.io.tmpdir") + "/" + xmlFileName);
      java.nio.file.Files.write(xmlFile.toPath(),
          java.util.Base64.getDecoder().decode(xmlBase64));

      File cdrFile = new File(
          System.getProperty("java.io.tmpdir") + "/" + cdrFileName);
      java.nio.file.Files.write(cdrFile.toPath(),
          java.util.Base64.getDecoder().decode(cdrBase64));

      String xmlUrl = googleDriveService.uploadFileWithPublicAccess(xmlFile, folderId);
      String cdrUrl = googleDriveService.uploadFileWithPublicAccess(cdrFile, folderId);

      xmlFile.delete();
      cdrFile.delete();

      return new String[]{xmlUrl, cdrUrl};
    } catch (Exception e) {
      log.error("Error subiendo XML/CDR a Drive: {}", e.getMessage(), e);
      return new String[]{null, null};
    }
  }

  // ========================================================================================
  // CONSULTA TICKETS PENDIENTES DE GUÍAS (job cada minuto)
  // ========================================================================================

  private void checkPendingGuideTickets() {
    List<RemissionGuide> guides = remissionGuideRepository.findByStatusAndDeletedAtIsNull("EN PROCESO");
    if (guides.isEmpty()) return;

    log.info("Consultando tickets pendientes de guías: {}", guides.size());

    for (RemissionGuide guide : guides) {
      try {
        CompanySendRequest empresa = buildCompanySendRequestWithGuia(guide.getCompany());
        GuiaValidRequest validRequest = new GuiaValidRequest();
        validRequest.setEmpresa(empresa);
        validRequest.setTicket(guide.getSunatTicket());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GuiaValidRequest> entity = new HttpEntity<>(validRequest, headers);

        String requestJson = serializeJson(validRequest);
        log.info("Consultando ticket guía {}-{}: {}", guide.getSeries(), guide.getSequence(), guide.getSunatTicket());

        LocalDateTime sentAt = LocalDateTime.now();
        ResponseEntity<FacturacionResponse> response =
            restTemplate.exchange(sunatGuiaValidUrl, HttpMethod.POST, entity, FacturacionResponse.class);

        processGuideResponse(guide, response);
        saveLog("job-ticket", "09",
            guide.getSeries(), guide.getSequence(),
            requestJson, serializeJson(response.getBody()),
            response.getStatusCode().value(), response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null && response.getBody().isSuccess(), sentAt);

      } catch (Exception e) {
        guide.setStatus("ERROR");
        guide.setSunatMessage("Error consultando ticket: " + e.getMessage());
        log.error("Error consultando ticket guía {}", guide.getId(), e);
      }

      guide.setUpdatedBy("job-ticket");
      remissionGuideRepository.save(guide);
    }
  }

  // ========================================================================================
  // CONSULTA TICKET GUÍA POR ID (endpoint manual — actualiza la guía)
  // ========================================================================================

  /**
   * Consulta el ticket de una guía por su ID, procesa la respuesta y actualiza el estado
   * en la base de datos igual que el job automático.
   */
  @org.springframework.transaction.annotation.Transactional
  public FacturacionResponse checkGuideTicketById(Long id) {
    RemissionGuide guide = remissionGuideRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ResourceNotFoundException("Guía de remisión no encontrada con id: " + id));

    if (guide.getSunatTicket() == null || guide.getSunatTicket().isBlank()) {
      throw new BusinessValidationException(
          "La guía " + guide.getSeries() + "-" + guide.getSequence() + " no tiene ticket registrado.");
    }

    CompanySendRequest empresa = buildCompanySendRequestWithGuia(guide.getCompany());

    GuiaValidRequest request = new GuiaValidRequest();
    request.setEmpresa(empresa);
    request.setTicket(guide.getSunatTicket());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<GuiaValidRequest> entity = new HttpEntity<>(request, headers);

    String requestJson = serializeJson(request);
    log.info("Consultando ticket manual guía {}-{}: {}", guide.getSeries(), guide.getSequence(), guide.getSunatTicket());

    LocalDateTime sentAt = LocalDateTime.now();
    ResponseEntity<FacturacionResponse> response =
        restTemplate.exchange(sunatGuiaValidUrl, HttpMethod.POST, entity, FacturacionResponse.class);

    processGuideResponse(guide, response);
    saveLog("manual-ticket", "09",
        guide.getSeries(), guide.getSequence(),
        requestJson, serializeJson(response.getBody()),
        response.getStatusCode().value(), response.getStatusCode().is2xxSuccessful()
            && response.getBody() != null && response.getBody().isSuccess(), sentAt);

    guide.setUpdatedBy("manual-ticket");
    remissionGuideRepository.save(guide);

    log.info("Estado guía {}-{} tras consulta manual: {}", guide.getSeries(), guide.getSequence(), guide.getStatus());
    return response.getBody();
  }

  // ========================================================================================
  // CONSULTA TICKET GUÍA (endpoint manual — solo consulta, sin actualizar)
  // ========================================================================================

  /**
   * Consulta el estado de un ticket de guía de remisión en el facturador externo.
   * Los datos de la empresa se obtienen de la configuración.
   */
  public FacturacionResponse checkGuideTicket(Company company, String ticket) {
    CompanySendRequest empresa = buildCompanySendRequestWithGuia(company);

    GuiaValidRequest request = new GuiaValidRequest();
    request.setEmpresa(empresa);
    request.setTicket(ticket);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<GuiaValidRequest> entity = new HttpEntity<>(request, headers);

    log.info("Consultando ticket de guía: {}", ticket);
    ResponseEntity<FacturacionResponse> response =
        restTemplate.exchange(sunatGuiaValidUrl, HttpMethod.POST, entity, FacturacionResponse.class);
    log.info("Respuesta consulta ticket guía: {}", serializeJson(response.getBody()));

    return response.getBody();
  }
}
