package com.api.multiempresa.util;

import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PdfLogoResolver {

  private final GoogleDriveService googleDriveService;

  public String resolveLogoUrl(Company company) {
    if (company == null || company.getLogoUrl() == null || company.getLogoUrl().isBlank()) {
      throw new BusinessValidationException(
          "La empresa no tiene logo configurado. Configure el logo antes de generar comprobantes.");
    }
    try {
      String logoUrl = company.getLogoUrl();
      String fileId = logoUrl.substring(logoUrl.lastIndexOf('/') + 1);
      byte[] bytes = googleDriveService.downloadFileById(fileId);
      java.io.File tempFile = java.io.File.createTempFile("logo-", ".png");
      java.nio.file.Files.write(tempFile.toPath(), bytes);
      tempFile.deleteOnExit();
      return tempFile.toURI().toString();
    } catch (BusinessValidationException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error al descargar el logo de la empresa {}: {}", company.getRuc(), e.getMessage());
      throw new BusinessValidationException(
          "No se pudo obtener el logo de la empresa. Verifique la configuración e intente nuevamente.");
    }
  }
}
