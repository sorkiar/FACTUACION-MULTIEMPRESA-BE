package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.CreditDebitNote;
import com.api.multiempresa.dto.entity.CreditDebitNoteItem;
import com.api.multiempresa.dto.entity.CreditDebitNoteType;
import com.api.multiempresa.dto.entity.Document;
import com.api.multiempresa.dto.entity.DocumentSeries;
import com.api.multiempresa.dto.entity.DocumentTypeSunat;
import com.api.multiempresa.dto.entity.Product;
import com.api.multiempresa.dto.entity.Sale;
import com.api.multiempresa.dto.filter.CreditDebitNoteFilter;
import com.api.multiempresa.dto.mapper.CreditDebitNoteMapper;
import com.api.multiempresa.dto.request.CreditDebitNoteItemRequest;
import com.api.multiempresa.dto.request.CreditDebitNoteRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CreditDebitNoteResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CreditDebitNoteItemRepository;
import com.api.multiempresa.repository.CreditDebitNoteRepository;
import com.api.multiempresa.repository.CreditDebitNoteTypeRepository;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.DocumentRepository;
import com.api.multiempresa.repository.DocumentSeriesRepository;
import com.api.multiempresa.repository.DocumentTypeSunatRepository;
import com.api.multiempresa.repository.ProductRepository;
import com.api.multiempresa.repository.SaleRepository;
import com.api.multiempresa.repository.ServiceRepository;
import com.api.multiempresa.repository.spec.CreditDebitNoteSpecification;
import com.api.multiempresa.job.SunatDocumentJobService;
import com.api.multiempresa.service.CreditDebitNotePdfService;
import com.api.multiempresa.service.CreditDebitNoteService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditDebitNoteServiceImpl implements CreditDebitNoteService {

  private final CreditDebitNoteRepository noteRepository;
  private final CreditDebitNoteItemRepository noteItemRepository;
  private final CreditDebitNoteTypeRepository noteTypeRepository;
  private final CompanyRepository companyRepository;
  private final SaleRepository saleRepository;
  private final DocumentRepository documentRepository;
  private final DocumentSeriesRepository documentSeriesRepository;
  private final DocumentTypeSunatRepository documentTypeSunatRepository;
  private final ProductRepository productRepository;
  private final ServiceRepository serviceRepository;
  private final CreditDebitNoteMapper mapper;
  private final CreditDebitNotePdfService creditDebitNotePdfService;
  private final SunatDocumentJobService sunatDocumentJobService;
  private final com.api.multiempresa.service.SunatSendConfigService sunatSendConfigService;

  @Override
  public ApiResponse<List<CreditDebitNoteResponse>> findAll(CreditDebitNoteFilter filter) {
    List<CreditDebitNote> notes = noteRepository.findAll(
        CreditDebitNoteSpecification.byFilter(filter));
    return new ApiResponse<>("Notas listadas correctamente", mapper.toResponseList(notes));
  }

  @Override
  public ApiResponse<CreditDebitNoteResponse> findById(Long id) {
    Long companyId = TenantContext.getCurrentCompanyId();
    CreditDebitNote note = (companyId != null
        ? noteRepository.findByIdAndCompany_IdAndDeletedAtIsNull(id, companyId)
        : noteRepository.findByIdAndDeletedAtIsNull(id))
        .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada"));
    return new ApiResponse<>("Nota obtenida correctamente", mapper.toResponse(note));
  }

  @Override
  @Transactional
  public ApiResponse<CreditDebitNoteResponse> create(CreditDebitNoteRequest request) {

    Long companyId = TenantContext.getCurrentCompanyId();
    String username = JwtUtils.extractUsernameFromContext();

    // 1. Buscar el tipo de nota (C01-C13 / D01-D12)
    CreditDebitNoteType noteType = noteTypeRepository
        .findByCodeAndStatusNot(request.getNoteTypeCode().toUpperCase(), 0)
        .orElseThrow(() -> new BusinessValidationException(
            "Tipo de nota inválido: " + request.getNoteTypeCode()
                + ". Use C01-C13 para crédito o D01-D12 para débito."));

    // 2. Determinar tipo de documento SUNAT (07 = crédito, 08 = débito)
    String documentTypeCode = "CREDITO".equals(noteType.getNoteCategory()) ? "07" : "08";

    // 3. Obtener la venta original (scoped a la empresa del usuario)
    Sale sale = saleRepository.findByIdAndCompany_IdAndDeletedAtIsNull(request.getSaleId(), companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

    // 3.1. Verificar que la venta no tenga una NC de anulación (C01) vigente
    boolean yaAnulada = noteRepository
        .existsBySale_IdAndCreditDebitNoteType_CodeAndStatusInAndDeletedAtIsNull(
            sale.getId(), "C01", List.of("PENDIENTE", "ACEPTADO"));
    if (yaAnulada) {
      throw new BusinessValidationException(
          "La venta ya tiene una nota de crédito de anulación (C01) aplicada. "
              + "No se puede emitir más notas sobre esta venta.");
    }

    // 4. Obtener y validar el documento original (factura o boleta)
    Document originalDocument = documentRepository.findById(request.getOriginalDocumentId())
        .orElseThrow(() -> new ResourceNotFoundException("Documento original no encontrado"));

    if (!originalDocument.getSale().getId().equals(sale.getId())) {
      throw new BusinessValidationException(
          "El documento original no pertenece a la venta indicada");
    }

    if (!"ACEPTADO".equals(originalDocument.getStatus())) {
      throw new BusinessValidationException(
          "Solo se puede emitir notas sobre documentos aceptados por SUNAT. "
              + "Estado actual: " + originalDocument.getStatus());
    }

    String origDocTypeCode = originalDocument.getDocumentTypeSunat().getCode();
    if (!"01".equals(origDocTypeCode) && !"03".equals(origDocTypeCode)) {
      throw new BusinessValidationException(
          "El documento original debe ser una Factura (01) o Boleta (03)");
    }

    // 5. Obtener el tipo de documento SUNAT para la nota
    DocumentTypeSunat documentTypeSunat = documentTypeSunatRepository
        .findById(documentTypeCode)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Tipo de documento SUNAT no encontrado: " + documentTypeCode));

    // 6. Reservar secuencia en la serie (scoped a la empresa del usuario)
    DocumentSeries series = documentSeriesRepository
        .findByIdAndCompanyIdForUpdate(request.getDocumentSeriesId(), companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Serie no encontrada"));

    if (!documentTypeCode.equals(series.getDocumentTypeSunat().getCode())) {
      throw new BusinessValidationException(
          "La serie seleccionada no corresponde al tipo de nota ("
              + ("07".equals(documentTypeCode) ? "Nota de Crédito" : "Nota de Débito") + ")");
    }

    String seriesParent = series.getParentDocumentTypeCode();
    if (seriesParent != null && !seriesParent.equals(origDocTypeCode)) {
      String expectedLabel = "01".equals(seriesParent) ? "Factura (01)" : "Boleta (03)";
      String actualLabel = "01".equals(origDocTypeCode) ? "Factura (01)" : "Boleta (03)";
      throw new BusinessValidationException(
          "La serie " + series.getSeries() + " es para " + expectedLabel
              + " pero el documento original es una " + actualLabel + ".");
    }

    Integer nextSequence = series.getCurrentSequence() + 1;
    series.setCurrentSequence(nextSequence);
    documentSeriesRepository.save(series);

    // 7. Crear la nota
    CreditDebitNote note = new CreditDebitNote();
    note.setCompany(companyRepository.getReferenceById(companyId));
    note.setSale(sale);
    note.setOriginalDocument(originalDocument);
    note.setDocumentTypeSunat(documentTypeSunat);
    note.setDocumentSeries(series);
    note.setSeries(series.getSeries());
    note.setSequence(String.format("%08d", nextSequence));
    note.setIssueDate(LocalDateTime.now());
    note.setCreditDebitNoteType(noteType);
    note.setReason(request.getReason());
    note.setCurrencyCode(sale.getCurrencyCode() != null ? sale.getCurrencyCode() : "PEN");
    note.setTaxPercentage(sale.getTaxPercentage());
    note.setStatus("PENDIENTE");
    note.setCreatedBy(username);

    note = noteRepository.save(note);

    // 8. Procesar items y calcular totales
    BigDecimal subtotal = BigDecimal.ZERO;

    BigDecimal taxRate = new BigDecimal("0.18");
    BigDecimal hundred = new BigDecimal("100");

    for (CreditDebitNoteItemRequest itemReq : request.getItems()) {

      BigDecimal discountPct = itemReq.getDiscountPercentage() != null
          ? itemReq.getDiscountPercentage()
          : BigDecimal.ZERO;

      // lineTotal = qty × unitPrice × (1 - disc/100) — no intermediate rounding
      BigDecimal lineTotal = itemReq.getQuantity()
          .multiply(itemReq.getUnitPrice())
          .multiply(hundred.subtract(discountPct))
          .divide(hundred, 10, RoundingMode.HALF_UP);

      BigDecimal itemTax = lineTotal.multiply(taxRate);
      BigDecimal itemTotal = lineTotal.add(itemTax);

      BigDecimal itemBase = lineTotal.setScale(2, RoundingMode.HALF_UP);
      itemTax = itemTax.setScale(2, RoundingMode.HALF_UP);

      CreditDebitNoteItem item = new CreditDebitNoteItem();
      item.setCreditDebitNote(note);
      item.setItemType(itemReq.getItemType());
      item.setDescription(itemReq.getDescription());
      item.setQuantity(itemReq.getQuantity());
      item.setUnitPrice(itemReq.getUnitPrice());
      item.setDiscountPercentage(discountPct);
      item.setSubtotalAmount(itemBase);
      item.setTaxAmount(itemTax);
      item.setTotalAmount(itemTotal.setScale(2, RoundingMode.HALF_UP));
      item.setCreatedBy(username);

      if ("PRODUCTO".equals(itemReq.getItemType())) {
        if (itemReq.getProductId() == null) {
          throw new BusinessValidationException("productId es obligatorio cuando itemType=PRODUCTO");
        }
        Product product = productRepository
            .findByIdAndCompany_IdAndStatusNot(itemReq.getProductId(), companyId, 0)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        item.setProduct(product);
      }

      if ("SERVICIO".equals(itemReq.getItemType())) {
        if (itemReq.getServiceId() == null) {
          throw new BusinessValidationException("serviceId es obligatorio cuando itemType=SERVICIO");
        }
        com.api.multiempresa.dto.entity.Service service = serviceRepository
            .findByIdAndCompany_IdAndStatusNot(itemReq.getServiceId(), companyId, 0)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        item.setService(service);
      }

      noteItemRepository.save(item);

      subtotal = subtotal.add(lineTotal);  // accumulate unrounded
    }

    // Global totals — round only when persisting (HALF_UP, 2 dec)
    BigDecimal igv = subtotal.multiply(taxRate);
    BigDecimal total = subtotal.add(igv);

    note.setSubtotalAmount(subtotal.setScale(2, RoundingMode.HALF_UP));
    note.setTaxAmount(igv.setScale(2, RoundingMode.HALF_UP));
    note.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    noteRepository.save(note);

    creditDebitNotePdfService.generatePdf(note.getId());

    // Envío inmediato solo si el tipo está en modo ONLINE
    String modoKey = "07".equals(documentTypeCode) ? "nota_credito" : "nota_debito";
    if (sunatSendConfigService.isOnlineMode(modoKey)) {
      sunatDocumentJobService.sendCreditDebitNoteNow(note);
    }

    CreditDebitNote saved = noteRepository.findByIdAndDeletedAtIsNull(note.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada"));

    String tipoNota = "CREDITO".equals(noteType.getNoteCategory()) ? "crédito" : "débito";
    return new ApiResponse<>(
        "Nota de " + tipoNota + " registrada correctamente",
        mapper.toResponse(saved));
  }
}
