package com.api.multiempresa.service.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.api.multiempresa.service.GoogleDriveService;
import com.api.multiempresa.util.GoogleDriveOAuthUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleDriveServiceImpl implements GoogleDriveService {

  private final Drive driveService;

  public GoogleDriveServiceImpl(
      @Value("${google.drive.tokens-directory:tokens}") String tokensDirectory
  ) throws Exception {
    NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    Credential credential =
        GoogleDriveOAuthUtils.getDefaultCredentials(httpTransport, tokensDirectory);

    this.driveService = new Drive.Builder(
        httpTransport,
        JacksonFactory.getDefaultInstance(),
        credential
    )
        .setApplicationName(GoogleDriveOAuthUtils.APPLICATION_NAME)
        .build();
  }

  // =====================================================================================
  // PUBLIC API
  // =====================================================================================

  @Override
  public String uploadPdf(java.io.File pdfFile, String folderId) throws IOException {
    return uploadOrUpdateFile(pdfFile, folderId, "application/pdf", false);
  }

  @Override
  public String uploadFileWithPublicAccess(java.io.File file, String folderId) throws IOException {
    String mimeType = detectMimeType(file);
    String fileId = uploadOrUpdateFile(file, folderId, mimeType, true);
    return getWebViewLink(fileId);
  }

  @Override
  public String uploadAnuncioImage(java.io.File file, String folderId) throws IOException {
    return uploadImageOptimized(file, folderId);
  }

  @Override
  public String uploadLoginBackgroundImage(java.io.File file, String folderId)
      throws IOException {
    return uploadImageOptimized(file, folderId);
  }

  @Override
  public byte[] downloadFileById(String fileId) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
    return outputStream.toByteArray();
  }

  // =====================================================================================
  // CORE LOGIC
  // =====================================================================================

  private String uploadOrUpdateFile(
      java.io.File file,
      String folderId,
      String mimeType,
      boolean makePublic
  ) throws IOException {

    String fileId = findExistingFileId(file.getName(), folderId);
    FileContent mediaContent = new FileContent(mimeType, file);

    if (fileId != null) {
      driveService.files()
          .update(fileId, null, mediaContent)
          .execute();
    } else {
      File metadata = new File();
      metadata.setName(file.getName());
      metadata.setParents(List.of(folderId));

      File uploaded = driveService.files()
          .create(metadata, mediaContent)
          .setFields("id")
          .execute();

      fileId = uploaded.getId();
    }

    if (makePublic) {
      makeFilePublic(fileId);
    }

    return fileId;
  }

  private String uploadImageOptimized(java.io.File file, String folderId)
      throws IOException {

    String mimeType = detectMimeType(file);
    String fileId = uploadOrUpdateFile(file, folderId, mimeType, true);

    return buildOptimizedImageUrl(fileId);
  }

  // =====================================================================================
  // DRIVE HELPERS
  // =====================================================================================

  private String findExistingFileId(String fileName, String folderId)
      throws IOException {

    String query = String.format(
        "name='%s' and '%s' in parents and trashed=false",
        fileName,
        folderId
    );

    FileList result = driveService.files()
        .list()
        .setQ(query)
        .setFields("files(id)")
        .execute();

    return result.getFiles().isEmpty()
        ? null
        : result.getFiles().get(0).getId();
  }

  private void makeFilePublic(String fileId) throws IOException {
    Permission permission = new Permission()
        .setType("anyone")
        .setRole("reader");

    driveService.permissions()
        .create(fileId, permission)
        .execute();
  }

  private String getWebViewLink(String fileId) throws IOException {
    return driveService.files()
        .get(fileId)
        .setFields("webViewLink")
        .execute()
        .getWebViewLink();
  }

  private String buildOptimizedImageUrl(String fileId) {
    return "https://lh3.googleusercontent.com/d/" + fileId;
  }

  // =====================================================================================
  // UTIL
  // =====================================================================================

  private String detectMimeType(java.io.File file) throws IOException {
    String mimeType = java.nio.file.Files.probeContentType(file.toPath());

    if (mimeType != null) {
      return mimeType;
    }

    return "application/octet-stream";
  }
}
