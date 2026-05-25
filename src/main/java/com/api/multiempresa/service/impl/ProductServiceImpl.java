package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Product;
import com.api.multiempresa.dto.filter.ProductFilter;
import com.api.multiempresa.dto.mapper.ProductMapper;
import com.api.multiempresa.dto.request.ProductRequest;
import com.api.multiempresa.dto.request.ProductStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ProductResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.dto.entity.DetractionCode;
import com.api.multiempresa.repository.CategoryRepository;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.DetractionCodeRepository;
import com.api.multiempresa.repository.ProductRepository;
import com.api.multiempresa.repository.UnitMeasureRepository;
import com.api.multiempresa.repository.spec.ProductSpecification;
import com.api.multiempresa.service.GoogleDriveService;
import com.api.multiempresa.service.ProductService;
import com.api.multiempresa.service.SkuSequenceService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  private final ProductRepository repository;
  private final CategoryRepository categoryRepository;
  private final UnitMeasureRepository unitMeasureRepository;
  private final DetractionCodeRepository detractionCodeRepository;
  private final ProductMapper mapper;
  private final GoogleDriveService googleDriveService;
  private final SkuSequenceService skuSequenceService;
  private final CompanyRepository companyRepository;

  @Value("${drive.folder-id.productos-imagenes}")
  private String PRODUCT_IMAGE_FOLDER_ID;

  @Value("${drive.folder-id.productos-fichas}")
  private String PRODUCT_TECH_SHEET_FOLDER_ID;

  @Override
  public ApiResponse<List<ProductResponse>> findAll(ProductFilter filter) {
    return new ApiResponse<>(
        "Productos listados correctamente",
        mapper.toResponseList(repository.findAll(ProductSpecification.byFilter(filter)))
    );
  }

  @Override
  @Transactional
  public ApiResponse<ProductResponse> create(
      ProductRequest request,
      MultipartFile mainImage,
      MultipartFile technicalSheet
  ) {
    Long companyId = TenantContext.getCurrentCompanyId();
    String sku = skuSequenceService.registerSku("PRD");

    if (repository.existsBySkuAndCompanyId(sku, companyId)) {
      throw new BusinessValidationException("El SKU ya existe");
    }

    try {
      String imageUrl = null;

      if (mainImage != null && !mainImage.isEmpty()) {
        File imageFile = convertMultipartToFile(mainImage, sku + "_image");
        imageUrl = googleDriveService.uploadFileWithPublicAccess(imageFile, PRODUCT_IMAGE_FOLDER_ID);
      }

      String technicalSheetUrl = null;

      if (technicalSheet != null && !technicalSheet.isEmpty()) {
        File pdfFile = convertMultipartToFile(
            technicalSheet,
            sku + "_tech_sheet"
        );

        String fileId = googleDriveService.uploadPdf(
            pdfFile,
            PRODUCT_TECH_SHEET_FOLDER_ID
        );

        technicalSheetUrl =
            "https://drive.google.com/file/d/" + fileId + "/view";
      }

      Product product = new Product();
      validatePrices(request.getSalePricePen(), request.getSalePriceUsd());

      product.setSku(sku);
      product.setName(request.getName());
      product.setCategory(categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada")));
      product.setUnitMeasure(unitMeasureRepository.findById(request.getUnitMeasureId())
          .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada")));
      product.setSalePricePen(request.getSalePricePen());
      product.setSalePriceUsd(request.getSalePriceUsd());
      product.setEstimatedCostPen(request.getEstimatedCostPen());
      product.setEstimatedCostUsd(request.getEstimatedCostUsd());
      product.setBrand(request.getBrand());
      product.setModel(request.getModel());
      product.setShortDescription(request.getShortDescription());
      product.setTechnicalSpec(request.getTechnicalSpec());
      product.setMainImageUrl(imageUrl);
      product.setTechnicalSheetUrl(technicalSheetUrl);
      product.setDetractionCode(resolveDetractionCode(request.getDetractionCodeId()));
      product.setStatus(1);
      product.setCompany(companyRepository.getReferenceById(companyId));
      product.setCreatedBy(JwtUtils.extractUsernameFromContext());

      return new ApiResponse<>(
          "Producto registrado correctamente",
          mapper.toResponse(repository.save(product))
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
  public ApiResponse<ProductResponse> update(
      Long id,
      ProductRequest request,
      MultipartFile mainImage,
      MultipartFile technicalSheet
  ) {
    Product product = repository.findByIdAndStatusNot(id, 2)
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

    try {
      if (mainImage != null && !mainImage.isEmpty()) {
        File imageFile = convertMultipartToFile(
            mainImage,
            product.getSku() + "_image"
        );

        String imageUrl = googleDriveService.uploadFileWithPublicAccess(
            imageFile,
            PRODUCT_IMAGE_FOLDER_ID
        );

        product.setMainImageUrl(imageUrl);
      }

      if (technicalSheet != null && !technicalSheet.isEmpty()) {
        File pdfFile = convertMultipartToFile(
            technicalSheet,
            product.getSku() + "_tech_sheet"
        );

        String fileId = googleDriveService.uploadPdf(
            pdfFile,
            PRODUCT_TECH_SHEET_FOLDER_ID
        );

        String technicalSheetUrl =
            "https://drive.google.com/file/d/" + fileId + "/view";

        product.setTechnicalSheetUrl(technicalSheetUrl);
      }

      validatePrices(request.getSalePricePen(), request.getSalePriceUsd());

      product.setName(request.getName());
      product.setSalePricePen(request.getSalePricePen());
      product.setSalePriceUsd(request.getSalePriceUsd());
      product.setEstimatedCostPen(request.getEstimatedCostPen());
      product.setEstimatedCostUsd(request.getEstimatedCostUsd());
      product.setBrand(request.getBrand());
      product.setModel(request.getModel());
      product.setShortDescription(request.getShortDescription());
      product.setTechnicalSpec(request.getTechnicalSpec());
      product.setDetractionCode(resolveDetractionCode(request.getDetractionCodeId()));
      product.setUpdatedBy(JwtUtils.extractUsernameFromContext());

      return new ApiResponse<>(
          "Producto actualizado correctamente",
          mapper.toResponse(repository.save(product))
      );

    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessValidationException(
          "Error al actualizar archivos del producto: " + e.getMessage()
      );
    }
  }

  @Override
  @Transactional
  public ApiResponse<Void> updateStatus(Long id, ProductStatusRequest request) {

    Product product = repository.findByIdAndStatusNot(id, 2)
        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

    product.setStatus(request.getStatus());
    product.setUpdatedBy(JwtUtils.extractUsernameFromContext());

    repository.save(product);
    return new ApiResponse<>("Estado del producto actualizado", null);
  }

  private DetractionCode resolveDetractionCode(Long detractionCodeId) {
    if (detractionCodeId == null) {
      return null;
    }
    DetractionCode detraction = detractionCodeRepository.findById(detractionCodeId)
        .orElseThrow(() -> new ResourceNotFoundException("Código de detracción no encontrado"));
    if (!"BIEN".equals(detraction.getCategory())) {
      throw new BusinessValidationException(
          "El código de detracción seleccionado no corresponde a un bien (categoría BIEN)");
    }
    return detraction;
  }

  private void validatePrices(BigDecimal pricePen, BigDecimal priceUsd) {
    boolean penPositive = pricePen != null && pricePen.compareTo(BigDecimal.ZERO) > 0;
    boolean usdPositive = priceUsd != null && priceUsd.compareTo(BigDecimal.ZERO) > 0;
    if (!penPositive && !usdPositive) {
      throw new BusinessValidationException(
          "Debe registrar al menos un precio de venta mayor a 0 (soles o dólares)");
    }
  }

  private File convertMultipartToFile(
      MultipartFile multipart,
      String prefix
  ) throws IOException {

    File file = File.createTempFile(prefix, "");
    // File file = File.createTempFile(prefix, "_" + multipart.getOriginalFilename());
    multipart.transferTo(file);
    return file;
  }
}
