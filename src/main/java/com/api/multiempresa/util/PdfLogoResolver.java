package com.api.multiempresa.util;

import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.service.GoogleDriveService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PdfLogoResolver {

  private final GoogleDriveService googleDriveService;

  public String resolveLogoUrl(Company company) {
    String defaultLogo = Objects.requireNonNull(
        getClass().getResource("/img/logo.png")).toString();
    if (company == null || company.getLogoUrl() == null || company.getLogoUrl().isBlank()) {
      return defaultLogo;
    }
    try {
      String logoUrl = company.getLogoUrl();
      String fileId = logoUrl.substring(logoUrl.lastIndexOf('/') + 1);
      byte[] bytes = googleDriveService.downloadFileById(fileId);
      java.io.File tempFile = java.io.File.createTempFile("logo-", ".png");
      java.nio.file.Files.write(tempFile.toPath(), bytes);
      tempFile.deleteOnExit();
      return tempFile.toURI().toString();
    } catch (Exception e) {
      log.warn("No se pudo descargar el logo de la empresa {}, usando logo por defecto: {}",
          company.getRuc(), e.getMessage());
      return defaultLogo;
    }
  }
}
