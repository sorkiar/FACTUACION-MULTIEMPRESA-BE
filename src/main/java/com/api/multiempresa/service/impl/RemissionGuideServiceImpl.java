package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Carrier;
import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.entity.Client;
import com.api.multiempresa.dto.entity.ClientAddress;
import com.api.multiempresa.dto.entity.DocumentSeries;
import com.api.multiempresa.dto.entity.Driver;
import com.api.multiempresa.dto.entity.DriverVehicle;
import com.api.multiempresa.dto.entity.RemissionGuide;
import com.api.multiempresa.dto.entity.RemissionGuideDriver;
import com.api.multiempresa.dto.entity.RemissionGuideItem;
import com.api.multiempresa.dto.entity.TransferReason;
import com.api.multiempresa.dto.filter.RemissionGuideFilter;
import com.api.multiempresa.dto.mapper.RemissionGuideMapper;
import com.api.multiempresa.dto.request.RemissionGuideDriverRequest;
import com.api.multiempresa.dto.request.RemissionGuideItemRequest;
import com.api.multiempresa.dto.request.RemissionGuideRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.RemissionGuideResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.job.SunatDocumentJobService;
import com.api.multiempresa.repository.CarrierRepository;
import com.api.multiempresa.repository.ClientAddressRepository;
import com.api.multiempresa.repository.ClientRepository;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.DocumentSeriesRepository;
import com.api.multiempresa.repository.DriverRepository;
import com.api.multiempresa.repository.DriverVehicleRepository;
import com.api.multiempresa.repository.ProductRepository;
import com.api.multiempresa.repository.RemissionGuideDriverRepository;
import com.api.multiempresa.repository.RemissionGuideItemRepository;
import com.api.multiempresa.repository.RemissionGuideRepository;
import com.api.multiempresa.repository.TransferReasonRepository;
import com.api.multiempresa.repository.spec.RemissionGuideSpecification;
import com.api.multiempresa.service.RemissionGuidePdfService;
import com.api.multiempresa.service.RemissionGuideService;
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
public class RemissionGuideServiceImpl implements RemissionGuideService {

  private final RemissionGuideRepository guideRepository;
  private final RemissionGuideItemRepository itemRepository;
  private final RemissionGuideDriverRepository driverRepository;
  private final CompanyRepository companyRepository;
  private final DocumentSeriesRepository documentSeriesRepository;
  private final ProductRepository productRepository;
  private final ClientRepository clientRepository;
  private final ClientAddressRepository clientAddressRepository;
  private final CarrierRepository carrierRepository;
  private final DriverRepository driverMasterRepository;
  private final DriverVehicleRepository vehicleRepository;
  private final TransferReasonRepository transferReasonRepository;
  private final RemissionGuideMapper mapper;
  private final RemissionGuidePdfService pdfService;
  private final SunatDocumentJobService sunatDocumentJobService;
  private final com.api.multiempresa.service.SunatSendConfigService sunatSendConfigService;

  @Override
  public ApiResponse<List<RemissionGuideResponse>> findAll(RemissionGuideFilter filter) {
    List<RemissionGuide> guides = guideRepository.findAll(
        RemissionGuideSpecification.byFilter(filter));
    return new ApiResponse<>("Guías listadas correctamente", mapper.toResponseList(guides));
  }

  @Override
  public ApiResponse<RemissionGuideResponse> findById(Long id) {
    Long companyId = TenantContext.getCurrentCompanyId();
    RemissionGuide guide = (companyId != null
        ? guideRepository.findByIdAndCompany_IdAndDeletedAtIsNull(id, companyId)
        : guideRepository.findByIdAndDeletedAtIsNull(id))
        .orElseThrow(() -> new ResourceNotFoundException("Guía de remisión no encontrada"));
    return new ApiResponse<>("Guía obtenida correctamente", mapper.toResponse(guide));
  }

  @Override
  @Transactional
  public ApiResponse<RemissionGuideResponse> create(RemissionGuideRequest request) {

    Long companyId = TenantContext.getCurrentCompanyId();
    String username = JwtUtils.extractUsernameFromContext();
    boolean amazoniaLaw = companyRepository.findById(companyId)
        .map(Company::getSunatAmazoniaLaw)
        .map(Boolean.TRUE::equals)
        .orElse(false);

    // 1. Validaciones de negocio
    if ("TRANSPORTE_PRIVADO".equals(request.getTransportMode())) {
      if (request.getDrivers() == null || request.getDrivers().isEmpty()) {
        throw new BusinessValidationException(
            "Debe registrar al menos un conductor para TRANSPORTE_PRIVADO");
      }
    }

    if ("TRANSPORTE_PUBLICO".equals(request.getTransportMode())) {
      if (request.getCarrierId() == null) {
        throw new BusinessValidationException(
            "Se requiere carrierId para TRANSPORTE_PUBLICO");
      }
    }

    // 2. Reservar secuencia en la serie (con lock pesimista, scoped a la empresa)
    DocumentSeries series = documentSeriesRepository
        .findActiveByDocumentTypeCodeAndCompanyForUpdate("09", 1, companyId)
        .stream().findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(
            "No se encontró una serie activa para Guía de Remisión (09)"));

    Integer nextSequence = series.getCurrentSequence() + 1;
    series.setCurrentSequence(nextSequence);
    documentSeriesRepository.save(series);

    // 3. Crear la guía
    RemissionGuide guide = new RemissionGuide();
    guide.setCompany(companyRepository.getReferenceById(companyId));
    guide.setDocumentSeries(series);
    guide.setSeries(series.getSeries());
    guide.setSequence(String.format("%08d", nextSequence));
    guide.setIssueDate(LocalDateTime.now());
    guide.setTransferDate(request.getTransferDate());

    TransferReason transferReason = transferReasonRepository.findById(request.getTransferReasonId())
        .orElseThrow(() -> new ResourceNotFoundException("Motivo de traslado no encontrado"));
    if ("13".equals(transferReason.getCode())
        && (request.getTransferReasonDescription() == null
            || request.getTransferReasonDescription().isBlank())) {
      throw new BusinessValidationException(
          "transferReasonDescription es obligatorio cuando el motivo de traslado es 'Otros'");
    }
    guide.setTransferReason(transferReason);
    guide.setTransferReasonDescription(request.getTransferReasonDescription());
    guide.setTransportMode(request.getTransportMode());
    guide.setGrossWeight(request.getGrossWeight());
    guide.setWeightUnit(request.getWeightUnit() != null ? request.getWeightUnit() : "KGM");
    guide.setPackageCount(request.getPackageCount() != null ? request.getPackageCount() : 1);
    guide.setOriginAddress(request.getOriginAddress());
    guide.setOriginUbigeo(request.getOriginUbigeo());
    guide.setOriginLocalCode(request.getOriginLocalCode());
    guide.setDestinationAddress(request.getDestinationAddress());
    guide.setDestinationUbigeo(request.getDestinationUbigeo());
    guide.setDestinationLocalCode(request.getDestinationLocalCode());
    guide.setMinorVehicleTransfer(
        request.getMinorVehicleTransfer() != null && request.getMinorVehicleTransfer());

    // Destinatario (cliente, scoped a la empresa)
    Client client = clientRepository.findByIdAndCompany_IdAndDeletedAtIsNull(
            request.getClientId(), companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Destinatario no encontrado"));
    guide.setClient(client);

    // Dirección del cliente para el comprobante (texto obligatorio del request)
    guide.setClientAddress(request.getClientAddress());

    // Dirección registrada del cliente (referencial, nullable)
    if (request.getClientAddressId() != null) {
      ClientAddress selectedAddress = clientAddressRepository
          .findByIdAndClientIdAndDeletedAtIsNull(request.getClientAddressId(), client.getId())
          .orElseThrow(
              () -> new ResourceNotFoundException("Dirección del destinatario no encontrada"));
      guide.setSelectedClientAddress(selectedAddress);
    }

    // Transportista (TRANSPORTE_PUBLICO → master carrier, scoped a la empresa)
    if (request.getCarrierId() != null) {
      Carrier carrier = carrierRepository
          .findByIdAndCompany_IdAndStatusNot(request.getCarrierId(), companyId, 0)
          .orElseThrow(() -> new ResourceNotFoundException("Transportista no encontrado o inactivo"));
      guide.setCarrier(carrier);
    }

    guide.setObservations(request.getObservations());
    guide.setStatus("PENDIENTE");
    guide.setCreatedBy(username);

    guide = guideRepository.save(guide);

    // 4. Guardar ítems
    for (RemissionGuideItemRequest itemReq : request.getItems()) {
      RemissionGuideItem item = new RemissionGuideItem();
      item.setRemissionGuide(guide);
      item.setDescription(itemReq.getDescription());
      item.setQuantity(itemReq.getQuantity());
      item.setUnitMeasureSunat(
          itemReq.getUnitMeasureSunat() != null ? itemReq.getUnitMeasureSunat() : "NIU");

      BigDecimal unitPrice = itemReq.getUnitPrice();
      BigDecimal valorUnitario = amazoniaLaw
          ? unitPrice
          : unitPrice.divide(new BigDecimal("1.18"), 6, RoundingMode.HALF_UP);
      BigDecimal subtotal = valorUnitario.multiply(itemReq.getQuantity())
          .setScale(2, RoundingMode.HALF_UP);
      BigDecimal tax = amazoniaLaw
          ? BigDecimal.ZERO
          : subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
      BigDecimal total = subtotal.add(tax);

      item.setUnitPrice(unitPrice);
      item.setSubtotalAmount(subtotal);
      item.setTaxAmount(tax);
      item.setTotalAmount(total);
      item.setCreatedBy(username);

      if (itemReq.getProductId() != null) {
        item.setProduct(productRepository
            .findByIdAndCompany_IdAndStatusNot(itemReq.getProductId(), companyId, 0)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado")));
      }

      itemRepository.save(item);
    }

    // 5. Guardar conductores (TRANSPORTE_PRIVADO → master driver + specific vehicle)
    if (request.getDrivers() != null) {
      for (RemissionGuideDriverRequest driverReq : request.getDrivers()) {
        Driver driver = driverMasterRepository
            .findByIdAndCompany_IdAndStatusNot(driverReq.getDriverId(), companyId, 0)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Conductor no encontrado o inactivo: id=" + driverReq.getDriverId()));

        RemissionGuideDriver guideDriver = new RemissionGuideDriver();
        guideDriver.setRemissionGuide(guide);
        guideDriver.setDriver(driver);

        if (driverReq.getVehiclePlateId() != null) {
          DriverVehicle vehicle = vehicleRepository
              .findByIdAndDriverIdAndDeletedAtIsNull(driverReq.getVehiclePlateId(), driver.getId())
              .orElseThrow(() -> new ResourceNotFoundException(
                  "Placa no encontrada para el conductor id=" + driverReq.getDriverId()));
          guideDriver.setDriverVehicle(vehicle);
          guideDriver.setVehiclePlate(vehicle.getPlate());
        } else {
          guideDriver.setVehiclePlate(driverReq.getVehiclePlate());
        }

        guideDriver.setCreatedBy(username);
        driverRepository.save(guideDriver);
      }
    }

    // 6. Generar PDF
    pdfService.generatePdf(guide.getId());

    // 7. Enviar a SUNAT solo si está en modo ONLINE
    if (sunatSendConfigService.isOnlineMode("guia_remision")) {
      sunatDocumentJobService.sendRemissionGuideNow(guide);
    }

    RemissionGuide saved = guideRepository.findByIdAndDeletedAtIsNull(guide.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Guía de remisión no encontrada"));

    return new ApiResponse<>("Guía de remisión registrada correctamente", mapper.toResponse(saved));
  }
}
