package com.api.multiempresa.service;

import java.io.File;
import java.io.IOException;

public interface GoogleDriveService {
  String uploadPdf(File pdfFile, String folderId) throws IOException;

  String uploadFileWithPublicAccess(File file, String folderId) throws IOException;

  String uploadAnuncioImage(File file, String folderId) throws IOException;

  String uploadLoginBackgroundImage(File file, String folderId) throws IOException;

  byte[] downloadFileById(String fileId) throws IOException;
}