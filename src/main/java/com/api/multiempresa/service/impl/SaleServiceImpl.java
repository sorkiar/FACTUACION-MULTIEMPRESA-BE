package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Client;
import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.entity.DetractionCode;
import com.api.multiempresa.repository.DetractionCodeRepository;
import com.api.multiempresa.dto.entity.Document;
import com.api.multiempresa.dto.entity.DocumentSeries;
import com.api.multiempresa.dto.entity.ExchangeRate;
import com.api.multiempresa.dto.entity.PaymentMethod;
import com.api.multiempresa.dto.entity.Product;
import com.api.multiempresa.dto.entity.Sale;
import com.api.multiempresa.dto.entity.SaleInstallment;
import com.api.multiempresa.dto.entity.SaleItem;
import com.api.multiempresa.dto.entity.SalePayment;
import com.api.multiempresa.dto.filter.SaleFilter;
import com.api.multiempresa.dto.mapper.SaleMapper;
import com.api.multiempresa.dto.request.SaleInstallmentRequest;
import com.api.multiempresa.dto.request.SaleItemRequest;
import com.api.multiempresa.dto.request.SalePaymentRequest;
import com.api.multiempresa.dto.request.SaleRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.SaleResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.job.SunatDocumentJobService;
import com.api.multiempresa.repository.ClientRepository;
import com.api.multiempresa.repository.DocumentRepository;
import com.api.multiempresa.repository.DocumentSeriesRepository;
import com.api.multiempresa.repository.ExchangeRateRepository;
import com.api.multiempresa.repository.PaymentMethodRepository;
import com.api.multiempresa.repository.ProductRepository;
import com.api.multiempresa.dto.entity.SaleRelatedGuide;
import com.api.multiempresa.repository.SaleInstallmentRepository;
import com.api.multiempresa.repository.SaleItemRepository;
import com.api.multiempresa.repository.SalePaymentRepository;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.SaleRelatedGuideRepository;
import com.api.multiempresa.repository.SaleRepository;
import com.api.multiempresa.repository.ServiceRepository;
import com.api.multiempresa.repository.spec.SaleSpecification;
import com.api.multiempresa.service.ConfigurationService;
import com.api.multiempresa.service.DocumentPdfService;
import com.api.multiempresa.service.GoogleDriveService;
import com.api.multiempresa.util.PdfLogoResolver;
import com.api.multiempresa.service.SaleService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import jakarta.transaction.Transactional;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

  private final CompanyRepository companyRepository;
  private final SaleRepository saleRepository;
  private final SaleItemRepository saleItemRepository;
  private final SaleInstallmentRepository saleInstallmentRepository;
  private final SaleRelatedGuideRepository saleRelatedGuideRepository;
  private final ClientRepository clientRepository;
  private final ProductRepository productRepository;
  private final ServiceRepository serviceRepository;
  private final PaymentMethodRepository paymentMethodRepository;
  private final DocumentSeriesRepository documentSeriesRepository;
  private final SalePaymentRepository salePaymentRepository;
  private final DocumentRepository documentRepository;
  private final ExchangeRateRepository exchangeRateRepository;
  private final DetractionCodeRepository detractionCodeRepository;
  private final SaleMapper mapper;
  private final DocumentPdfService documentPdfService;
  private final GoogleDriveService googleDriveService;
  private final ConfigurationService configurationService;
  private final PdfLogoResolver pdfLogoResolver;
  private final SunatDocumentJobService sunatDocumentJobService;
  private final com.api.multiempresa.service.SunatSendConfigService sunatSendConfigService;

  @Value("${drive.folder-id.pagos}")
  private String PAYMENT_PROOF_FOLDER_ID;

  @Override
  @Transactional
  public ApiResponse<List<SaleResponse>> findAll(SaleFilter filter) {
    // @Transactional mantiene la Session abierta para que @BatchSize inicialice
    // las colecciones lazy (items, documents, payments, installments, relatedGuides)
    // con queries agrupadas en lugar de un producto cartesiano.
    return new ApiResponse<>(
        "Ventas listadas correctamente",
        mapper.toResponseList(
            saleRepository.findAll(SaleSpecification.byFilter(filter))
        )
    );
  }

  @Override
  @Transactional
  public ApiResponse<SaleResponse> create(
      SaleRequest request,
      MultiValueMap<String, MultipartFile> paymentProofs
  ) {

    Long companyId = TenantContext.getCurrentCompanyId();
    String username = JwtUtils.extractUsernameFromContext();

    Sale sale = new Sale();
    Company company = companyRepository.findById(companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    sale.setCompany(company);
    boolean amazoniaLaw = Boolean.TRUE.equals(company.getSunatAmazoniaLaw());
    sale.setCurrencyCode(resolveCurrencyCode(request.getCurrencyCode()));
    sale.setTaxPercentage(new BigDecimal("18"));
    sale.setCreatedBy(username);
    sale.setSaleDate(LocalDateTime.now());
    sale.setPaymentType(resolvePaymentType(request.getPaymentType()));
    sale.setPurchaseOrder(request.getPurchaseOrder());
    sale.setObservations(request.getObservations());

    Client client = clientRepository.findById(request.getClientId())
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    sale.setClient(client);
    sale.setClientAddressId(request.getClientAddressId());
    sale.setClientAddress(request.getClientAddress());

    if (Boolean.TRUE.equals(request.getDraft())) {
      sale.setSaleStatus("BORRADOR");
    } else {
      sale.setSaleStatus("CANCELADO");
    }

    sale = saleRepository.save(sale);

    Totals totals = rebuildItemsAndTotals(sale, request.getItems(), username, amazoniaLaw);

    sale.setSubtotalAmount(totals.subtotal());
    sale.setTaxAmount(totals.tax());
    sale.setTotalAmount(totals.total());
    sale = saleRepository.save(sale);

    processRelatedGuides(sale, request.getRelatedGuides());

    //  Si es borrador → termina aquí
    if (Boolean.TRUE.equals(request.getDraft())) {
      return new ApiResponse<>("Venta guardada como borrador",
          mapper.toResponse(sale));
    }

    // Calcular retención y detracción antes de validar pagos/cuotas
    computeRetention(sale, request.getDocumentSeriesId());
    computeDetraction(sale, request.getDocumentSeriesId());
    sale = saleRepository.save(sale);

    // Finalización real: pagos o cuotas según condición de pago
    if ("CREDITO".equals(sale.getPaymentType())) {
      processInstallments(sale, request, username);
    } else {
      processPayments(sale, request, paymentProofs, username);
    }
    Document document = generateDocument(sale, request.getDocumentSeriesId(), username);

    // Envío inmediato solo si el tipo está en modo ONLINE
    String docTypeCode = document.getDocumentTypeSunat().getCode();
    String modoKey = "01".equals(docTypeCode) ? "factura" : "boleta";
    if (sunatSendConfigService.isOnlineMode(modoKey)) {
      sunatDocumentJobService.sendDocumentNow(document);
    }

    Sale saleWithRelations = saleRepository
        .findByIdAndDeletedAtIsNull(sale.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

    return new ApiResponse<>("Venta registrada correctamente",
        mapper.toResponse(saleWithRelations));
  }

  @Override
  @Transactional
  public ApiResponse<SaleResponse> updateDraft(
      Long id,
      SaleRequest request,
      MultiValueMap<String, MultipartFile> paymentProofs
  ) {

    String username = JwtUtils.extractUsernameFromContext();

    Sale sale = saleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

    if (!"BORRADOR".equals(sale.getSaleStatus())) {
      throw new BusinessValidationException(
          "Solo se pueden actualizar ventas en estado BORRADOR");
    }

    if (request.getClientId() != null) {
      Client client = clientRepository.findById(request.getClientId())
          .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
      sale.setClient(client);
    }
    if (request.getClientAddressId() != null) {
      sale.setClientAddressId(request.getClientAddressId());
    }
    if (request.getClientAddress() != null) {
      sale.setClientAddress(request.getClientAddress());
    }

    sale.setCurrencyCode(resolveCurrencyCode(request.getCurrencyCode()));
    sale.setPaymentType(resolvePaymentType(request.getPaymentType()));
    sale.setPurchaseOrder(request.getPurchaseOrder());
    sale.setObservations(request.getObservations());

    saleItemRepository.deleteBySaleId(sale.getId());

    boolean amazoniaLaw = Boolean.TRUE.equals(sale.getCompany().getSunatAmazoniaLaw());
    Totals totals = rebuildItemsAndTotals(sale, request.getItems(), username, amazoniaLaw);

    sale.setSubtotalAmount(totals.subtotal());
    sale.setTaxAmount(totals.tax());
    sale.setTotalAmount(totals.total());
    sale.setUpdatedBy(username);
    sale = saleRepository.save(sale);

    processRelatedGuides(sale, request.getRelatedGuides());

    // Calcular retención y detracción antes de validar pagos/cuotas
    computeRetention(sale, request.getDocumentSeriesId());
    computeDetraction(sale, request.getDocumentSeriesId());
    sale = saleRepository.save(sale);

    //  Finalizar borrador: pagos o cuotas según condición de pago
    if ("CREDITO".equals(sale.getPaymentType())) {
      processInstallments(sale, request, username);
    } else {
      processPayments(sale, request, paymentProofs, username);
    }
    Document document = generateDocument(sale, request.getDocumentSeriesId(), username);

    String docTypeCode = document.getDocumentTypeSunat().getCode();
    String modoKey = "01".equals(docTypeCode) ? "factura" : "boleta";
    if (sunatSendConfigService.isOnlineMode(modoKey)) {
      sunatDocumentJobService.sendDocumentNow(document);
    }

    sale.setSaleStatus("CANCELADO");
    saleRepository.save(sale);

    Sale saleWithRelations = saleRepository
        .findByIdAndDeletedAtIsNull(sale.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

    return new ApiResponse<>("Venta finalizada correctamente",
        mapper.toResponse(saleWithRelations));
  }

  // ============================================================
  // Helpers de lógica de negocio
  // ============================================================

  private Totals rebuildItemsAndTotals(Sale sale, List<SaleItemRequest> items, String username, boolean amazoniaLaw) {

    if (items == null || items.isEmpty()) {
      throw new BusinessValidationException("Debe registrar al menos un item");
    }

    BigDecimal subtotal = BigDecimal.ZERO;

    BigDecimal taxRate = new BigDecimal("0.18");
    BigDecimal hundred = new BigDecimal("100");

    List<SaleItem> itemsToSave = new ArrayList<>();

    for (SaleItemRequest itemReq : items) {

      BigDecimal discountPct = itemReq.getDiscountPercentage() != null
          ? itemReq.getDiscountPercentage()
          : BigDecimal.ZERO;

      // lineTotal = qty × unitPrice × (1 - disc/100) — no intermediate rounding
      BigDecimal lineTotal = itemReq.getQuantity()
          .multiply(itemReq.getUnitPrice())
          .multiply(hundred.subtract(discountPct))
          .divide(hundred, 10, RoundingMode.HALF_UP);

      BigDecimal itemTax = amazoniaLaw ? BigDecimal.ZERO : lineTotal.multiply(taxRate);
      BigDecimal itemTotal = lineTotal.add(itemTax);

      SaleItem item = new SaleItem();

      if ("PRODUCTO".equals(itemReq.getItemType())) {
        if (itemReq.getProductId() == null) {
          throw new BusinessValidationException("productId es obligatorio cuando itemType=PRODUCTO");
        }
        Product product = productRepository.findById(itemReq.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        item.setProduct(product);
      }

      if ("SERVICIO".equals(itemReq.getItemType())) {
        if (itemReq.getServiceId() == null) {
          throw new BusinessValidationException("serviceId es obligatorio cuando itemType=SERVICIO");
        }
        com.api.multiempresa.dto.entity.Service service = serviceRepository.findById(itemReq.getServiceId())
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        item.setService(service);
      }

      if (itemReq.getDetractionCodeId() != null) {
        DetractionCode dc = detractionCodeRepository.findById(itemReq.getDetractionCodeId())
            .orElseThrow(() -> new ResourceNotFoundException("Código de detracción no encontrado"));
        item.setDetractionCode(dc);
      }

      item.setSale(sale);
      item.setItemType(itemReq.getItemType());
      item.setDescription(itemReq.getDescription());
      item.setQuantity(itemReq.getQuantity());
      item.setUnitPrice(itemReq.getUnitPrice());
      item.setDiscountPercentage(discountPct);
      item.setSubtotalAmount(lineTotal.setScale(2, RoundingMode.HALF_UP));
      item.setTaxAmount(itemTax.setScale(2, RoundingMode.HALF_UP));
      item.setTotalAmount(itemTotal.setScale(2, RoundingMode.HALF_UP));
      item.setCreatedBy(username);

      itemsToSave.add(item);

      subtotal = subtotal.add(lineTotal);  // accumulate unrounded
    }

    saleItemRepository.saveAll(itemsToSave);

    // Global totals — round only when persisting (HALF_UP, 2 dec)
    BigDecimal igv = amazoniaLaw ? BigDecimal.ZERO : subtotal.multiply(taxRate);
    BigDecimal total = subtotal.add(igv);

    return new Totals(
        subtotal.setScale(2, RoundingMode.HALF_UP),
        igv.setScale(2, RoundingMode.HALF_UP),
        total.setScale(2, RoundingMode.HALF_UP)
    );
  }

  private void processPayments(
      Sale sale,
      SaleRequest request,
      MultiValueMap<String, MultipartFile> paymentProofs,
      String username
  ) {

    if (request.getPayments() == null || request.getPayments().isEmpty()) {
      throw new BusinessValidationException("Debe registrar al menos un pago");
    }

    BigDecimal total = getExpectedAmount(sale);
    BigDecimal totalPaid = BigDecimal.ZERO;
    SalePayment lastCashPayment = null;

    salePaymentRepository.deleteBySaleId(sale.getId());

    for (SalePaymentRequest paymentReq : request.getPayments()) {

      PaymentMethod method = paymentMethodRepository
          .findById(paymentReq.getPaymentMethodId())
          .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado"));

      MultipartFile proofFile = null;

      if (paymentReq.getProofKey() != null
          && paymentProofs != null
          && paymentProofs.containsKey(paymentReq.getProofKey())) {

        proofFile = paymentProofs.getFirst(paymentReq.getProofKey());
      }

      // CASH nunca permite archivo
      if ("CASH".equals(method.getCode())
          && proofFile != null && !proofFile.isEmpty()) {

        throw new BusinessValidationException(
            "El método CASH no debe tener comprobante");
      }

      String proofUrl = null;

      if (proofFile != null && !proofFile.isEmpty()) {
        proofUrl = uploadPaymentProofToDrive(proofFile, username);
      }

      SalePayment payment = new SalePayment();
      payment.setSale(sale);
      payment.setPaymentMethod(method);
      payment.setAmountPaid(paymentReq.getAmountPaid());
      payment.setPaymentReference(paymentReq.getPaymentReference());
      payment.setProofFileUrl(proofUrl);
      payment.setPaymentDate(LocalDateTime.now());
      payment.setChangeAmount(BigDecimal.ZERO);
      payment.setCreatedBy(username);

      payment = salePaymentRepository.save(payment);

      totalPaid = totalPaid.add(paymentReq.getAmountPaid());

      if ("CASH".equals(method.getCode())) {
        lastCashPayment = payment;
      }
    }

    if (totalPaid.compareTo(total) < 0) {
      throw new BusinessValidationException(
          "El total pagado no cubre el total de la venta");
    }

    if (totalPaid.compareTo(total) > 0) {

      BigDecimal change = totalPaid.subtract(total)
          .setScale(2, RoundingMode.HALF_UP);

      if (lastCashPayment == null) {
        throw new BusinessValidationException(
            "Si hay vuelto, debe existir un pago en efectivo (CASH)");
      }

      lastCashPayment.setChangeAmount(change);
      lastCashPayment.setUpdatedBy(username);
      salePaymentRepository.save(lastCashPayment);
    }
  }

  private Document generateDocument(
      Sale sale,
      Long documentSeriesId,
      String username
  ) {

    if (documentSeriesId == null) {
      throw new BusinessValidationException(
          "documentSeriesId es obligatorio al finalizar la venta");
    }

    DocumentSeries series = documentSeriesRepository
        .findByIdAndCompanyIdForUpdate(documentSeriesId, sale.getCompany().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Serie no encontrada"));

    Integer nextSequence = series.getCurrentSequence() + 1;
    series.setCurrentSequence(nextSequence);
    documentSeriesRepository.save(series);

    String formattedSequence = String.format("%08d", nextSequence);

    Document document = new Document();
    document.setSale(sale);
    document.setCompany(sale.getCompany());
    document.setDocumentSeries(series);
    document.setDocumentTypeSunat(series.getDocumentTypeSunat());
    document.setSeries(series.getSeries());
    document.setSequence(formattedSequence);
    document.setIssueDate(LocalDateTime.now());
    document.setStatus("PENDIENTE");
    document.setCreatedBy(username);

    documentRepository.save(document);

    documentPdfService.generatePdf(sale.getId());

    return document;
  }

  private String uploadPaymentProofToDrive(
      MultipartFile file,
      String username
  ) {
    try {
      String originalName = file.getOriginalFilename();
      String extension = "";

      if (originalName != null && originalName.contains(".")) {
        extension = originalName.substring(originalName.lastIndexOf("."));
      }

      String safePrefix =
          "sale_payment_" + System.currentTimeMillis();

      File temp = convertMultipartToFile(file, safePrefix + extension);

      return googleDriveService.uploadFileWithPublicAccess(
          temp,
          PAYMENT_PROOF_FOLDER_ID
      );

    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessValidationException(
          "Error al subir comprobante de pago: " + e.getMessage()
      );
    }
  }

  @Override
  public byte[] generateQuotation(SaleRequest request) {
    Client client = clientRepository.findById(request.getClientId())
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    Company quotationCompany = companyRepository.findById(TenantContext.getCurrentCompanyId()).orElse(null);

    Map<String, String> config = configurationService.getGroup("empresa_emisora");

    List<Map<String, Object>> dataList = new ArrayList<>();
    BigDecimal taxRate = new BigDecimal("0.18");

    String cotRef = "COT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    String clientName = buildClientName(client);

    for (SaleItemRequest itemReq : request.getItems()) {
      Map<String, Object> row = new HashMap<>();

      // Empresa
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

      // Documento
      row.put("tico_descripcion", "COTIZACIÓN");
      row.put("comp_numero_comprobante", cotRef);
      row.put("comp_fecha_emicion", Timestamp.valueOf(LocalDateTime.now()));
      row.put("comp_estado", "ACTIVO");
      row.put("comp_descuento_global", 0);
      row.put("comp_condicion_pago", "-");
      row.put("observaciones", null);
      row.put("priorizar_despacho", false);
      row.put("comp_codigo_hash", "");
      row.put("comp_cadena_qr", "");

      // Cliente
      row.put("comp_descripcion_cliente", clientName);
      row.put("clie_numero_documento", client.getDocumentNumber());
      row.put("comp_direccion_cliente", request.getClientAddress());

      // Moneda
      row.put("comp_descripcion_moneda", request.getCurrencyCode().equalsIgnoreCase("PEN") ? "SOLES" : "DÓLARES");
      row.put("comp_simbolo_moneda", request.getCurrencyCode().equalsIgnoreCase("PEN") ? "S/" : "$");

      // Item
      String sku = "CUST0000";
      String unidad = "NIU";
      if ("PRODUCTO".equals(itemReq.getItemType()) && itemReq.getProductId() != null) {
        Product p = productRepository.findById(itemReq.getProductId()).orElse(null);
        if (p != null) {
          sku = p.getSku();
          unidad = p.getUnitMeasure().getCodeSunat();
        }
      } else if ("SERVICIO".equals(itemReq.getItemType()) && itemReq.getServiceId() != null) {
        com.api.multiempresa.dto.entity.Service s =
            serviceRepository.findById(itemReq.getServiceId()).orElse(null);
        if (s != null) {
          sku = s.getSku();
        }
      }

      BigDecimal itemBase = itemReq.getQuantity()
          .multiply(itemReq.getUnitPrice())
          .setScale(2, RoundingMode.HALF_UP);
      BigDecimal itemTax = itemBase.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

      row.put("itco_codigo_interno", sku);
      row.put("itco_descripcion_completa", itemReq.getDescription());
      row.put("itco_cantidad", itemReq.getQuantity().doubleValue());
      row.put("itco_unidad_medida", unidad);
      row.put("itco_precio_unitario", itemReq.getUnitPrice().doubleValue());
      BigDecimal discountPct = itemReq.getDiscountPercentage() != null ? itemReq.getDiscountPercentage() : BigDecimal.ZERO;
      BigDecimal discountAmount = itemBase.multiply(discountPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      row.put("itco_descuento", discountAmount);
      row.put("itco_tipo_igv", 10);
      row.put("itco_igv", itemTax);
      row.put("otros_tributos", 0.0);
      row.put("icbper", 0.0);

      // Campos adicionales que requiere el template
      row.put("numero_placa", null);
      row.put("marc_descripcion", null);
      row.put("exonerado_igv", null);
      row.put("comp_vendedor", null);
      row.put("comp_id", null);

      dataList.add(row);
    }

    try {
      InputStream inputStream = getClass().getResourceAsStream("/jasper/CotizacionA4.jrxml");
      JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
      JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("urlImagen", pdfLogoResolver.resolveLogoUrl(quotationCompany));

      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      JasperExportManager.exportReportToPdfStream(jasperPrint, out);
      return out.toByteArray();

    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessValidationException("Error al generar PDF de cotización: " + e.getMessage());
    }
  }

  private String buildClientName(Client client) {
    if (client.getBusinessName() != null && !client.getBusinessName().isBlank()) {
      return client.getBusinessName();
    }
    String fullName =
        (client.getFirstName() != null ? client.getFirstName() : "") + " " +
            (client.getLastName() != null ? client.getLastName() : "");
    return fullName.trim();
  }

  private File convertMultipartToFile(MultipartFile multipart, String prefix) throws IOException {
    File file = File.createTempFile(prefix, "");
    multipart.transferTo(file);
    return file;
  }

  private void processInstallments(Sale sale, SaleRequest request, String username) {

    if (request.getInstallments() == null || request.getInstallments().isEmpty()) {
      throw new BusinessValidationException(
          "Las cuotas son obligatorias para venta a crédito");
    }

    BigDecimal sumCuotas = request.getInstallments().stream()
        .map(SaleInstallmentRequest::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);

    // Monto neto esperado: total menos retención (si aplica) menos detracción en moneda local (si aplica)
    // retentionAmount está en PEN; usar tasa para obtener equivalente en moneda de la venta
    BigDecimal expectedTotal = sale.getTotalAmount();
    if (Boolean.TRUE.equals(sale.getHasRetention()) && sale.getRetentionRate() != null) {
      BigDecimal retLocal = sale.getTotalAmount()
          .multiply(sale.getRetentionRate())
          .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      expectedTotal = expectedTotal.subtract(retLocal);
    }
    if (Boolean.TRUE.equals(sale.getHasDetraction()) && sale.getDetractionRate() != null) {
      BigDecimal detrLocal = sale.getTotalAmount()
          .multiply(sale.getDetractionRate())
          .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      expectedTotal = expectedTotal.subtract(detrLocal);
    }
    if (sumCuotas.subtract(expectedTotal).abs().compareTo(new BigDecimal("0.01")) > 0) {
      throw new BusinessValidationException(
          "La suma de las cuotas (" + sumCuotas + ") debe ser igual al monto neto de la venta ("
              + expectedTotal + ")");
    }

    LocalDate saleDate = sale.getSaleDate().toLocalDate();
    for (SaleInstallmentRequest installmentReq : request.getInstallments()) {
      LocalDate dueDate = LocalDate.parse(installmentReq.getDueDate());
      if (!dueDate.isAfter(saleDate)) {
        throw new BusinessValidationException(
            "La fecha de vencimiento de cada cuota debe ser posterior a la fecha de la venta");
      }
    }

    saleInstallmentRepository.deleteBySaleId(sale.getId());

    List<SaleInstallment> installmentsToSave = new ArrayList<>();
    for (SaleInstallmentRequest installmentReq : request.getInstallments()) {
      SaleInstallment installment = new SaleInstallment();
      installment.setSale(sale);
      installment.setInstallmentNumber(installmentReq.getInstallmentNumber());
      installment.setDueDate(LocalDate.parse(installmentReq.getDueDate()));
      installment.setAmount(installmentReq.getAmount());
      installment.setCreatedBy(username);
      installmentsToSave.add(installment);
    }
    saleInstallmentRepository.saveAll(installmentsToSave);
  }

  private String resolveCurrencyCode(String currencyCode) {
    if ("USD".equalsIgnoreCase(currencyCode)) {
      return "USD";
    }
    return "PEN";
  }

  private String resolvePaymentType(String paymentType) {
    if ("CREDITO".equalsIgnoreCase(paymentType)) {
      return "CREDITO";
    }
    return "CONTADO";
  }

  private void processRelatedGuides(Sale sale, List<String> guideNumbers) {
    saleRelatedGuideRepository.deleteBySaleId(sale.getId());
    if (guideNumbers == null || guideNumbers.isEmpty()) return;
    List<SaleRelatedGuide> guidesToSave = new ArrayList<>();
    for (String number : guideNumbers) {
      SaleRelatedGuide guide = new SaleRelatedGuide();
      guide.setSale(sale);
      guide.setGuideNumber(number.trim());
      guidesToSave.add(guide);
    }
    saleRelatedGuideRepository.saveAll(guidesToSave);
  }

  /**
   * Calcula y aplica la retención a la venta si corresponde:
   *  - Solo para FACTURA (código "01")
   *  - Solo si el cliente es agente de retención
   *  - Solo si el total en PEN supera el monto mínimo configurado
   *  - El monto de retención se almacena SIEMPRE en PEN (soles)
   */
  private void computeRetention(Sale sale, Long documentSeriesId) {
    sale.setHasRetention(false);
    sale.setRetentionAmount(null);
    sale.setRetentionRate(null);

    if (documentSeriesId == null) return;

    com.api.multiempresa.dto.entity.DocumentSeries series =
        documentSeriesRepository.findById(documentSeriesId).orElse(null);
    if (series == null || series.getDocumentTypeSunat() == null) return;

    // Solo aplica a FACTURA
    if (!"01".equals(series.getDocumentTypeSunat().getCode())) return;

    // El cliente debe ser agente de retención
    if (!Boolean.TRUE.equals(sale.getClient().getRetentionAgent())) return;

    Map<String, String> config = configurationService.getGroup("detraccion_retencion");
    BigDecimal minAmount = new BigDecimal(config.getOrDefault("min_retencion_amount", "700"));
    BigDecimal rate = new BigDecimal(config.getOrDefault("retencion_rate", "3"));

    // Verificar umbral en PEN (el mínimo configurado siempre es en PEN)
    BigDecimal totalInPen = sale.getTotalAmount();
    if ("USD".equals(sale.getCurrencyCode())) {
      List<ExchangeRate> rates = exchangeRateRepository.findByDate(LocalDate.now());
      BigDecimal exchangeRateValue = rates.stream()
          .filter(r -> "V".equals(r.getType()))
          .map(ExchangeRate::getValue)
          .findFirst()
          .orElse(BigDecimal.ONE);
      totalInPen = sale.getTotalAmount()
          .multiply(exchangeRateValue)
          .setScale(2, RoundingMode.HALF_UP);
    }

    if (totalInPen.compareTo(minAmount) <= 0) return;

    // Retención siempre en PEN: base en soles × tasa / 100
    BigDecimal retentionAmount = totalInPen
        .multiply(rate)
        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

    sale.setHasRetention(true);
    sale.setRetentionRate(rate);
    sale.setRetentionAmount(retentionAmount);
  }

  /**
   * Calcula y aplica la detracción (SPOT) a la venta si corresponde:
   * - Solo para FACTURA (código "01")
   * - Total en PEN > min_amount del código (1/2 UIT para Anexo 1, S/700 para Anexo 2 y servicios)
   * - Todos los ítems sujetos a detracción deben tener el mismo código
   * - detractionAmount se almacena siempre en PEN
   */
  private void computeDetraction(Sale sale, Long documentSeriesId) {
    sale.setHasDetraction(false);
    sale.setDetractionCode(null);
    sale.setDetractionRate(null);
    sale.setDetractionAmount(null);

    if (documentSeriesId == null) return;

    DocumentSeries series = documentSeriesRepository.findById(documentSeriesId).orElse(null);
    if (series == null) return;
    String docTypeCode = series.getDocumentTypeSunat().getCode();
    if (!"01".equals(docTypeCode) && !"03".equals(docTypeCode)) return;

    // Recopilar códigos de detracción de los ítems (load directly — in-memory set may be empty)
    List<SaleItem> saleItems = saleItemRepository.findBySaleIdAndDeletedAtIsNull(sale.getId());
    String foundCode = null;
    BigDecimal foundRate = null;
    BigDecimal foundMinAmount = new BigDecimal("700");
    boolean conflict = false;
    for (SaleItem item : saleItems) {
      DetractionCode dc = item.getDetractionCode() != null
          ? item.getDetractionCode()
          : (item.getProduct() != null
              ? item.getProduct().getDetractionCode()
              : (item.getService() != null ? item.getService().getDetractionCode() : null));
      if (dc != null && Integer.valueOf(1).equals(dc.getStatus())) {
        if (foundCode == null) {
          foundCode = dc.getCode();
          foundRate = dc.getPercentage();
          foundMinAmount = dc.getMinAmount() != null ? dc.getMinAmount() : new BigDecimal("700");
        } else if (!foundCode.equals(dc.getCode())) {
          conflict = true;
          break;
        }
      }
    }
    if (foundCode == null) return;
    if (conflict) throw new BusinessValidationException(
        "All items must have the same detraction code");

    // Verificar umbral en PEN (min_amount del código es siempre en PEN)
    BigDecimal totalInPen = sale.getTotalAmount();
    BigDecimal exchangeRateValue = BigDecimal.ONE;
    if ("USD".equals(sale.getCurrencyCode())) {
      List<ExchangeRate> rates = exchangeRateRepository.findByDate(LocalDate.now());
      exchangeRateValue = rates.stream()
          .filter(r -> "V".equals(r.getType()))
          .map(ExchangeRate::getValue)
          .findFirst()
          .orElse(BigDecimal.ONE);
      totalInPen = sale.getTotalAmount()
          .multiply(exchangeRateValue)
          .setScale(2, RoundingMode.HALF_UP);
    }

    if (totalInPen.compareTo(foundMinAmount) <= 0) return;

    // Monto de detracción siempre en PEN (con redondeo a 0.50 o entero)
    BigDecimal detrAmountPen = roundDetraction(
        totalInPen.multiply(foundRate).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));

    sale.setHasDetraction(true);
    sale.setDetractionCode(foundCode);
    sale.setDetractionRate(foundRate);
    sale.setDetractionAmount(detrAmountPen);

    // Detraction takes priority over retention: if detraction applies, retention must not
    sale.setHasRetention(false);
    sale.setRetentionAmount(null);
    sale.setRetentionRate(null);
  }

  /**
   * Monto esperado en pagos: total menos retención (si aplica) menos detracción en moneda
   * local (si aplica). Misma lógica que processInstallments().
   */
  private BigDecimal getExpectedAmount(Sale sale) {
    BigDecimal expected = sale.getTotalAmount();
    // retentionAmount está en PEN; usar tasa para equivalente en moneda de la venta
    if (Boolean.TRUE.equals(sale.getHasRetention()) && sale.getRetentionRate() != null) {
      BigDecimal retLocal = sale.getTotalAmount()
          .multiply(sale.getRetentionRate())
          .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      expected = expected.subtract(retLocal);
    }
    if (Boolean.TRUE.equals(sale.getHasDetraction()) && sale.getDetractionRate() != null) {
      BigDecimal detrLocal = sale.getTotalAmount()
          .multiply(sale.getDetractionRate())
          .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      expected = expected.subtract(detrLocal);
    }
    return expected;
  }

  /**
   * Redondeo especial para montos de detracción:
   * - Si la parte decimal es >= 0.5 → redondea a la unidad superior
   * - Si la parte decimal es < 0.5  → redondea a la unidad inferior (cero)
   */
  private BigDecimal roundDetraction(BigDecimal amount) {
    BigDecimal floor = amount.setScale(0, RoundingMode.FLOOR);
    BigDecimal decimal = amount.subtract(floor);
    if (decimal.compareTo(new BigDecimal("0.5")) >= 0) {
      return floor.add(BigDecimal.ONE).setScale(2, RoundingMode.UNNECESSARY);
    } else {
      return floor.setScale(2, RoundingMode.UNNECESSARY);
    }
  }

  // Helper interno simple para retornos de totales
  private record Totals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
  }
}
