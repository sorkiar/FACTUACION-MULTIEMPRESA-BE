package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.ChargeUnit;
import com.api.multiempresa.dto.entity.DetractionCode;
import com.api.multiempresa.dto.entity.ServiceCategory;
import com.api.multiempresa.dto.filter.ServiceFilter;
import com.api.multiempresa.dto.mapper.ServiceMapper;
import com.api.multiempresa.dto.request.ServiceRequest;
import com.api.multiempresa.dto.request.ServiceStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ServiceResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.ChargeUnitRepository;
import com.api.multiempresa.repository.DetractionCodeRepository;
import com.api.multiempresa.repository.ServiceCategoryRepository;
import com.api.multiempresa.repository.ServiceRepository;
import com.api.multiempresa.repository.spec.ServiceSpecification;
import com.api.multiempresa.service.ConfigurationService;
import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.service.GoogleDriveService;
import com.api.multiempresa.util.PdfLogoResolver;
import com.api.multiempresa.service.ServiceService;
import com.api.multiempresa.service.SkuSequenceService;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {
  private final ServiceRepository repository;
  private final ServiceCategoryRepository serviceCategoryRepository;
  private final ChargeUnitRepository chargeUnitRepository;
  private final DetractionCodeRepository detractionCodeRepository;
  private final ServiceMapper mapper;
  private final GoogleDriveService googleDriveService;
  private final SkuSequenceService skuSequenceService;
  private final ConfigurationService configurationService;
  private final CompanyRepository companyRepository;
  private final PdfLogoResolver pdfLogoResolver;

  @Value("${drive.folder-id.servicios-imagenes}")
  private String SERVICE_IMAGE_FOLDER_ID;

  @Value("${drive.folder-id.servicios-fichas}")
  private String SERVICE_TECH_SHEET_FOLDER_ID;

  @Override
  public ApiResponse<List<ServiceResponse>> findAll(ServiceFilter filter) {
    return new ApiResponse<>(
        "Servicios listados correctamente",
        mapper.toResponseList(
            repository.findAll(ServiceSpecification.byFilter(filter))
        )
    );
  }

  @Override
  @Transactional
  public ApiResponse<ServiceResponse> create(
      ServiceRequest request,
      MultipartFile image,
      MultipartFile technicalSheet
  ) {
    Long companyId = TenantContext.getCurrentCompanyId();

    ServiceCategory category = serviceCategoryRepository.findById(
        request.getServiceCategoryId()
    ).orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

    ChargeUnit chargeUnit = chargeUnitRepository.findById(
        request.getChargeUnitId()
    ).orElseThrow(() -> new ResourceNotFoundException("Unidad de cobro no encontrada"));

    String sku = skuSequenceService.registerSku("SRV");

    if (repository.existsBySkuAndCompanyId(sku, companyId)) {
      throw new BusinessValidationException("El SKU ya existe");
    }

    try {
      String imageUrl = null;
      if (image != null && !image.isEmpty()) {
        File imageFile = convertMultipartToFile(
            image,
            sku + "_image"
        );

        imageUrl = googleDriveService.uploadFileWithPublicAccess(
            imageFile,
            SERVICE_IMAGE_FOLDER_ID
        );
      }

      String technicalSheetUrl = null;
      if (technicalSheet != null && !technicalSheet.isEmpty()) {
        File pdfFile = convertMultipartToFile(
            technicalSheet,
            sku + "_tech_sheet"
        );

        String fileId = googleDriveService.uploadPdf(
            pdfFile,
            SERVICE_TECH_SHEET_FOLDER_ID
        );

        technicalSheetUrl =
            "https://drive.google.com/file/d/" + fileId + "/view";
      }

      com.api.multiempresa.dto.entity.Service service =
          new com.api.multiempresa.dto.entity.Service();

      validatePrices(request.getPricePen(), request.getPriceUsd());

      service.setSku(sku);
      service.setName(request.getName());
      service.setServiceCategory(category);
      service.setChargeUnit(chargeUnit);
      service.setPricePen(request.getPricePen());
      service.setPriceUsd(request.getPriceUsd());
      service.setEstimatedTime(request.getEstimatedTime());
      service.setExpectedDelivery(request.getExpectedDelivery());
      service.setIncludesDescription(request.getIncludesDescription());
      service.setExcludesDescription(request.getExcludesDescription());
      service.setConditions(request.getConditions());
      service.setRequiresMaterials(request.getRequiresMaterials());
      service.setRequiresSpecification(request.getRequiresSpecification());
      service.setShortDescription(request.getShortDescription());
      service.setDetailedDescription(request.getDetailedDescription());
      service.setImageUrl(imageUrl);
      service.setTechnicalSheetUrl(technicalSheetUrl);
      service.setDetractionCode(resolveDetractionCode(request.getDetractionCodeId()));
      service.setStatus(1);
      service.setCreatedBy(JwtUtils.extractUsernameFromContext());
      service.setCompany(companyRepository.getReferenceById(companyId));

      return new ApiResponse<>(
          "Servicio registrado correctamente",
          mapper.toResponse(repository.save(service))
      );

    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessValidationException(
          "Error al subir archivos a Google Drive: " + e.getMessage()
      );
    }
  }

  @Override
  @Transactional
  public ApiResponse<ServiceResponse> update(
      Long id,
      ServiceRequest request,
      MultipartFile image,
      MultipartFile technicalSheet
  ) {

    com.api.multiempresa.dto.entity.Service service =
        repository.findByIdAndStatusNot(id, 2)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

    try {
      if (image != null && !image.isEmpty()) {
        File imageFile = convertMultipartToFile(
            image,
            service.getSku() + "_image"
        );

        String imageUrl = googleDriveService.uploadFileWithPublicAccess(
            imageFile,
            SERVICE_IMAGE_FOLDER_ID
        );

        service.setImageUrl(imageUrl);
        imageFile.delete();
      }

      if (technicalSheet != null && !technicalSheet.isEmpty()) {
        File pdfFile = convertMultipartToFile(
            technicalSheet,
            service.getSku() + "_tech_sheet"
        );

        String fileId = googleDriveService.uploadPdf(
            pdfFile,
            SERVICE_TECH_SHEET_FOLDER_ID
        );

        String technicalSheetUrl =
            "https://drive.google.com/file/d/" + fileId + "/view";

        service.setTechnicalSheetUrl(technicalSheetUrl);
        pdfFile.delete();
      }

      validatePrices(request.getPricePen(), request.getPriceUsd());

      service.setName(request.getName());
      service.setPricePen(request.getPricePen());
      service.setPriceUsd(request.getPriceUsd());
      service.setEstimatedTime(request.getEstimatedTime());
      service.setExpectedDelivery(request.getExpectedDelivery());
      service.setIncludesDescription(request.getIncludesDescription());
      service.setExcludesDescription(request.getExcludesDescription());
      service.setConditions(request.getConditions());
      service.setRequiresMaterials(request.getRequiresMaterials());
      service.setRequiresSpecification(request.getRequiresSpecification());
      service.setShortDescription(request.getShortDescription());
      service.setDetailedDescription(request.getDetailedDescription());
      service.setDetractionCode(resolveDetractionCode(request.getDetractionCodeId()));
      service.setUpdatedBy(JwtUtils.extractUsernameFromContext());

      return new ApiResponse<>(
          "Servicio actualizado correctamente",
          mapper.toResponse(repository.save(service))
      );

    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessValidationException(
          "Error al actualizar archivos del servicio: " + e.getMessage()
      );
    }
  }

  @Override
  @Transactional
  public ApiResponse<Void> updateStatus(Long id, ServiceStatusRequest request) {
    com.api.multiempresa.dto.entity.Service service = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

    service.setStatus(request.getStatus());
    service.setUpdatedBy(JwtUtils.extractUsernameFromContext());

    repository.save(service);

    return new ApiResponse<>("Estado actualizado correctamente", null);
  }

  @Override
  public byte[] generatePdf(Long id) {
    com.api.multiempresa.dto.entity.Service service =
        repository.findByIdAndStatusNot(id, 2)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

    Company serviceCompany = companyRepository.findById(TenantContext.getCurrentCompanyId()).orElse(null);

    try {
      Map<String, String> config = configurationService.getGroup("empresa_emisora");

      Map<String, Object> row = new HashMap<>();
      row.put("empr_ruc", config.get("emprRuc"));
      row.put("empr_razon_social", config.get("emprRazonSocial"));
      row.put("empr_nombre_comercial", config.get("emprNombreComercial"));
      row.put("empr_direccion_fiscal", config.get("emprDireccionFiscal"));
      row.put("sku", service.getSku());
      row.put("name", service.getName());
      row.put("category", service.getServiceCategory() != null ? service.getServiceCategory().getName() : "");
      row.put("charge_unit", service.getChargeUnit() != null ? service.getChargeUnit().getName() : "");
      row.put("price", service.getPricePen() != null ? service.getPricePen().toPlainString() : "-");
      row.put("price_usd", service.getPriceUsd() != null ? service.getPriceUsd().toPlainString() : "-");
      row.put("estimated_time", service.getEstimatedTime());
      row.put("expected_delivery", service.getExpectedDelivery());
      row.put("requires_materials", Boolean.TRUE.equals(service.getRequiresMaterials()) ? "Sí" : "No");
      row.put("requires_specification", Boolean.TRUE.equals(service.getRequiresSpecification()) ? "Sí" : "No");
      row.put("short_description", service.getShortDescription());
      row.put("detailed_description", service.getDetailedDescription());
      row.put("includes_description", service.getIncludesDescription());
      row.put("excludes_description", service.getExcludesDescription());
      row.put("conditions", service.getConditions());
      row.put("image_url", service.getImageUrl());
      row.put("technical_sheet_url", service.getTechnicalSheetUrl());

      JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(List.of(row));

      InputStream inputStream = getClass().getResourceAsStream("/jasper/ServicioFichaA4.jrxml");
      JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("urlImagen", pdfLogoResolver.resolveLogoUrl(serviceCompany));

      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      JasperExportManager.exportReportToPdfStream(jasperPrint, out);
      return out.toByteArray();

    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessValidationException("Error al generar PDF del servicio: " + e.getMessage());
    }
  }

  private DetractionCode resolveDetractionCode(Long detractionCodeId) {
    if (detractionCodeId == null) {
      return null;
    }
    DetractionCode detraction = detractionCodeRepository.findById(detractionCodeId)
        .orElseThrow(() -> new ResourceNotFoundException("Código de detracción no encontrado"));
    if (!"SERVICIO".equals(detraction.getCategory())) {
      throw new BusinessValidationException(
          "El código de detracción seleccionado no corresponde a un servicio (categoría SERVICIO)");
    }
    return detraction;
  }

  private void validatePrices(BigDecimal pricePen, BigDecimal priceUsd) {
    boolean penPositive = pricePen != null && pricePen.compareTo(BigDecimal.ZERO) > 0;
    boolean usdPositive = priceUsd != null && priceUsd.compareTo(BigDecimal.ZERO) > 0;
    if (!penPositive && !usdPositive) {
      throw new BusinessValidationException(
          "Debe registrar al menos un precio mayor a 0 (soles o dólares)");
    }
  }

  private File convertMultipartToFile(
      MultipartFile multipart,
      String prefix
  ) throws IOException {

    File file = File.createTempFile(prefix, "");
    multipart.transferTo(file);
    return file;
  }

}
