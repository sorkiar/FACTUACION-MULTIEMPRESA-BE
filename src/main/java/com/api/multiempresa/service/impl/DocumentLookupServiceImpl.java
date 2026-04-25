package com.api.multiempresa.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.api.multiempresa.dto.entity.Configuration;
import com.api.multiempresa.dto.entity.DniRecord;
import com.api.multiempresa.dto.entity.RucRecord;
import com.api.multiempresa.dto.external.apiperu.ExternalApiResponse;
import com.api.multiempresa.dto.external.apiperu.ExternalDniData;
import com.api.multiempresa.dto.external.apiperu.ExternalRucData;
import com.api.multiempresa.dto.mapper.DniRecordMapper;
import com.api.multiempresa.dto.mapper.RucRecordMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DniRecordResponse;
import com.api.multiempresa.dto.response.RucRecordResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.repository.ConfigurationRepository;
import com.api.multiempresa.repository.DniRecordRepository;
import com.api.multiempresa.repository.RucRecordRepository;
import com.api.multiempresa.service.DocumentLookupService;
import com.api.multiempresa.util.JwtUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLookupServiceImpl implements DocumentLookupService {

  private static final String APIPERU_BASE_URL = "https://apiperu.dev/api";
  private static final String CONFIG_GROUP = "consulta_externa";
  private static final String CONFIG_KEY = "apiperu_token";
  private static final int CACHE_DAYS = 30;

  private final DniRecordRepository dniRecordRepository;
  private final RucRecordRepository rucRecordRepository;
  private final ConfigurationRepository configurationRepository;
  private final DniRecordMapper dniRecordMapper;
  private final RucRecordMapper rucRecordMapper;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;

  @Override
  public ApiResponse<DniRecordResponse> queryDni(String dni) {
    Optional<DniRecord> cached = dniRecordRepository.findById(dni);
    if (cached.isPresent() && isFresh(cached.get().getCreatedAt())) {
      return new ApiResponse<>("Consulta DNI exitosa", dniRecordMapper.toResponse(cached.get()));
    }

    String token = resolveToken();
    HttpHeaders headers = buildHeaders(token);

    ResponseEntity<String> response;
    try {
      response = restTemplate.exchange(
          APIPERU_BASE_URL + "/dni/" + dni,
          HttpMethod.GET,
          new HttpEntity<>(headers),
          String.class);
    } catch (Exception e) {
      log.error("Error comunicándose con la API externa para el DNI {}", dni, e);
      throw new BusinessValidationException("Error comunicándose con la API externa");
    }

    ExternalApiResponse<ExternalDniData> body;
    try {
      body = objectMapper.readValue(response.getBody(), new TypeReference<ExternalApiResponse<ExternalDniData>>() {
      });
    } catch (JsonProcessingException e) {
      log.error("Error deserializando la respuesta JSON para el DNI {}. Respuesta original: {}", dni,
          response.getBody(), e);
      throw new BusinessValidationException("A ocurrido un error procesando la estructura de datos");
    }
    if (body == null || body.getSuccess() == null) {
      throw new BusinessValidationException("La API externa no respondió correctamente");
    }
    if (!body.getSuccess()) {
      throw new BusinessValidationException(body.getMessage());
    }

    String username = JwtUtils.extractUsernameFromContext();
    DniRecord record = dniRecordMapper.toEntity(body.getData());
    if (cached.isEmpty()) {
      record.setCreatedBy(username);
    } else {
      record.setCreatedAt(cached.get().getCreatedAt());
      record.setCreatedBy(cached.get().getCreatedBy());
      record.setUpdatedBy(username);
    }
    record = dniRecordRepository.save(record);

    return new ApiResponse<>("Consulta DNI exitosa", dniRecordMapper.toResponse(record));
  }

  @Override
  public ApiResponse<RucRecordResponse> queryRuc(String ruc) {
    Optional<RucRecord> cached = rucRecordRepository.findById(ruc);
    if (cached.isPresent() && isFresh(cached.get().getCreatedAt())) {
      return new ApiResponse<>("Consulta RUC exitosa", rucRecordMapper.toResponse(cached.get()));
    }

    String token = resolveToken();
    HttpHeaders headers = buildHeaders(token);

    ResponseEntity<String> response;
    try {
      response = restTemplate.exchange(
          APIPERU_BASE_URL + "/ruc/" + ruc,
          HttpMethod.GET,
          new HttpEntity<>(headers),
          String.class);
    } catch (Exception e) {
      log.error("Error comunicándose con la API externa para el RUC {}", ruc, e);
      throw new BusinessValidationException("Error comunicándose con la API externa");
    }

    ExternalApiResponse<ExternalRucData> body;
    try {
      body = objectMapper.readValue(response.getBody(), new TypeReference<ExternalApiResponse<ExternalRucData>>() {
      });
    } catch (JsonProcessingException e) {
      log.error("Error deserializando la respuesta JSON para el RUC {}. Respuesta original: {}", ruc,
          response.getBody(), e);
      throw new BusinessValidationException("A ocurrido un error procesando la estructura de datos");
    }
    if (body == null || body.getSuccess() == null) {
      throw new BusinessValidationException("La API externa no respondió correctamente");
    }
    if (!body.getSuccess()) {
      throw new BusinessValidationException(body.getMessage());
    }

    String username = JwtUtils.extractUsernameFromContext();
    RucRecord record = rucRecordMapper.toEntity(body.getData());
    if (cached.isEmpty()) {
      record.setCreatedBy(username);
    } else {
      record.setCreatedAt(cached.get().getCreatedAt());
      record.setCreatedBy(cached.get().getCreatedBy());
      record.setUpdatedBy(username);
    }
    record = rucRecordRepository.save(record);

    return new ApiResponse<>("Consulta RUC exitosa", rucRecordMapper.toResponse(record));
  }

  private String resolveToken() {
    Configuration config = configurationRepository
        .findByConfigGroupAndConfigKeyAndDeletedAtIsNull(CONFIG_GROUP, CONFIG_KEY)
        .orElseThrow(() -> new BusinessValidationException("Token de consulta no configurado"));
    String token = config.getConfigValue();
    if (token == null || token.isBlank()) {
      throw new BusinessValidationException("Token de consulta no configurado");
    }
    return token;
  }

  private HttpHeaders buildHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private boolean isFresh(LocalDateTime createdAt) {
    return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(CACHE_DAYS));
  }
}
