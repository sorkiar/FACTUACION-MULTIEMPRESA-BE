package com.api.multiempresa.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public final class GoogleDriveOAuthUtils {

  private GoogleDriveOAuthUtils() {
  }

  public static final String APPLICATION_NAME = "IDMH Peru - Drive Service";

  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static final List<String> DEFAULT_SCOPES = List.of(DriveScopes.DRIVE_FILE);

  public static Credential getDefaultCredentials(
      NetHttpTransport httpTransport,
      String tokensDirectory
  ) throws Exception {
    return getCredentials(httpTransport, "/client_secret.json", tokensDirectory, 8888);
  }

  private static Credential getCredentials(
      NetHttpTransport httpTransport,
      String credentialsPath,
      String tokensDirectory,
      Integer receiverPort
  ) throws Exception {

    GoogleClientSecrets clientSecrets;

    try (InputStream in =
             GoogleDriveOAuthUtils.class.getResourceAsStream(credentialsPath)) {

      if (in == null) {
        throw new FileNotFoundException(
            "No se encontró el archivo de credenciales: " + credentialsPath
        );
      }

      clientSecrets =
          GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    }

    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            JSON_FACTORY,
            clientSecrets,
            DEFAULT_SCOPES
        )
            .setDataStoreFactory(
                new FileDataStoreFactory(new File(tokensDirectory))
            )
            .setAccessType("offline")
            .setApprovalPrompt("force")
            .build();

    LocalServerReceiver receiver = buildReceiver(receiverPort);

    return new AuthorizationCodeInstalledApp(flow, receiver)
        .authorize("user");
  }

  private static LocalServerReceiver buildReceiver(Integer port) {
    if (port == null) return new LocalServerReceiver();

    return new LocalServerReceiver.Builder()
        .setPort(port)
        .setCallbackPath("/Callback")
        .build();
  }
}
