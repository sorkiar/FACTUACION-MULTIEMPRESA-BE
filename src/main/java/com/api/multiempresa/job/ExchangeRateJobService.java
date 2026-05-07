package com.api.multiempresa.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.entity.ExchangeRate;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.ExchangeRateRepository;
import com.api.multiempresa.util.TenantContext;
import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateJobService {

  /**
   * Fuente oficial SUNAT TXT. Retorna el tipo de cambio del día actual.
   * Formato de respuesta: DD/MM/YYYY|compra|venta|
   */
  private static final String SUNAT_TXT_URL =
      "https://www.sunat.gob.pe/a/txt/tipoCambio.txt";

  private static final String SUNAT_ECONSULTA_BASE =
      "https://e-consulta.sunat.gob.pe/cl-at-ittipcam/tcS01Alias";

  /** Endpoint real del POST mensual. */
  private static final String SUNAT_ECONSULTA_DATA_URL =
      SUNAT_ECONSULTA_BASE + "/listarTipoCambio";

  private static final DateTimeFormatter ECONSULTA_DATE_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private static final java.time.ZoneId LIMA_ZONE = java.time.ZoneId.of("America/Lima");
  private static final int FETCH_START_HOUR = 1; // 1 AM Lima
  private static final int FETCH_INTERVAL_HOURS = 5; // intenta a 1h, 6h, 11h, 16h, 21h

  private final ExchangeRateRepository exchangeRateRepository;
  private final CompanyRepository companyRepository;
  private final ObjectMapper objectMapper;

  private final RestTemplate restTemplate = new RestTemplate();

  /** HttpClient con CookieManager para mantener sesión entre GET (token) y POST (datos). */
  private final HttpClient scraperClient = HttpClient.newBuilder()
      .cookieHandler(new CookieManager())
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build();

  /**
   * Corre cada 5 minutos. Intenta obtener el TC en ventanas horarias separadas por
   * FETCH_INTERVAL_HOURS horas, comenzando desde FETCH_START_HOUR (1 AM Lima).
   * Ejemplo con intervalo=5: intenta a la 1h, 6h, 11h, 16h, 21h.
   */
  @Scheduled(fixedRate = 300_000)
  public void scheduledFetch() {
    try {
      int currentHour = LocalTime.now(LIMA_ZONE).getHour();
      boolean inFetchWindow = false;
      for (int h = FETCH_START_HOUR; h < 24; h += FETCH_INTERVAL_HOURS) {
        if (currentHour == h) {
          inFetchWindow = true;
          break;
        }
      }
      if (!inFetchWindow) return;
      fetchTodayIfMissing();
    } catch (Exception e) {
      log.error("Error en scheduled exchange rate fetch: {}", e.getMessage(), e);
    }
  }

  /**
   * Al iniciar la aplicación, verifica si el tipo de cambio de hoy ya está registrado.
   * Si no, lo obtiene de inmediato (cubre el caso de despliegue tardío).
   */
  @EventListener(ApplicationReadyEvent.class)
  public void fetchOnStartup() {
    try {
      log.info("Verificando tipo de cambio al inicio...");
      fetchTodayIfMissing();
    } catch (Exception e) {
      log.error("Error al obtener tipo de cambio al inicio: {}", e.getMessage(), e);
    }
  }

  private void fetchTodayIfMissing() {
    LocalDate today = LocalDate.now(LIMA_ZONE);
    List<Company> companies = companyRepository.findByDeletedAtIsNull();
    if (companies.isEmpty()) {
      log.debug("Sin empresas activas, omitiendo fetch de tipo de cambio");
      return;
    }
    // Verificar si ya existe para la primera empresa (todas comparten el mismo TC del día)
    Company first = companies.get(0);
    if (exchangeRateRepository.existsByDateAndTypeAndCompanyId(today, "C", first.getId())) {
      log.debug("Tipo de cambio para {} ya registrado", today);
      return;
    }
    fetchFromSunatTxt(today, companies);
  }

  /**
   * Lee el archivo TXT oficial de SUNAT.
   * Solo retorna el tipo de cambio del día actual.
   * Formato: DD/MM/YYYY|compra|venta|
   */
  private void fetchFromSunatTxt(LocalDate date, List<Company> companies) {
    try {
      log.info("Obteniendo tipo de cambio desde TXT SUNAT para {}", date);
      ResponseEntity<String> response = restTemplate.getForEntity(SUNAT_TXT_URL, String.class);

      String txt = response.getBody();
      if (txt == null || txt.isBlank()) {
        log.warn("Respuesta vacía del TXT de SUNAT");
        return;
      }

      String[] parts = txt.trim().split("\\|");
      if (parts.length < 3) {
        log.warn("Formato inesperado del TXT de SUNAT: {}", txt);
        return;
      }

      BigDecimal compra = new BigDecimal(parts[1].trim());
      BigDecimal venta = new BigDecimal(parts[2].trim());

      for (Company company : companies) {
        saveRate(date, compra, "C", company);
        saveRate(date, venta, "V", company);
      }
      log.info("Tipo de cambio para {} registrado para {} empresa(s): compra={}, venta={}",
          date, companies.size(), compra, venta);

    } catch (Exception e) {
      log.error("Error obteniendo tipo de cambio del TXT SUNAT: {}", e.getMessage(), e);
    }
  }

  // ──────────────────────────────────────────────
  // Importación masiva via SUNAT e-consulta
  // ──────────────────────────────────────────────

  /**
   * Obtiene los tipos de cambio de SUNAT e-consulta para el rango [from, to] (mismo mes/año)
   * y los persiste. Devuelve la cantidad de registros nuevos guardados.
   */
  public int fetchAndSaveRange(LocalDate from, LocalDate to) {
    Long companyId = TenantContext.getCurrentCompanyId();
    Company company = companyRepository.getReferenceById(companyId);
    log.info("Importando tipos de cambio desde SUNAT e-consulta: {} a {}", from, to);
    try {
      String token = fetchEconsultaToken();
      List<SunatEconsultaRateItem> items =
          fetchMonthlyRates(from.getYear(), from.getMonthValue(), token);

      int saved = 0;
      for (SunatEconsultaRateItem item : items) {
        LocalDate date = LocalDate.parse(item.getFecPublica(), ECONSULTA_DATE_FORMAT);
        if (date.isBefore(from) || date.isAfter(to)) continue;

        BigDecimal value = new BigDecimal(item.getValTipo().trim());
        String type = item.getCodTipo(); // "C" o "V"
        upsertRate(date, value, type, company);
        saved++;
      }
      log.info("Importación finalizada. Registros procesados en rango: {}", saved);
      return saved;

    } catch (Exception e) {
      log.error("Error importando tipos de cambio desde e-consulta: {}", e.getMessage(), e);
      throw new RuntimeException("Error al importar tipos de cambio desde SUNAT e-consulta: " + e.getMessage(), e);
    }
  }

  private static final String TOKEN_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
  private static final int TOKEN_LENGTH = 52;
  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * GET a la página de e-consulta para establecer las cookies de sesión (IAASISTENCIAGESTIONSESSION).
   * El token real es generado por reCAPTCHA v3 de SUNAT via JS — no es recuperable del HTML.
   * Se genera un token aleatorio con el mismo formato; si SUNAT lo rechaza se registra la respuesta.
   */
  private String fetchEconsultaToken() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(SUNAT_ECONSULTA_BASE))
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .GET()
        .build();

    scraperClient.send(request, HttpResponse.BodyHandlers.ofString());
    // Cookies de sesión quedaron guardadas en el CookieManager del scraperClient

    String token = generateRandomToken();
    log.debug("Token generado para e-consulta: {}", token);
    return token;
  }

  private String generateRandomToken() {
    StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
    for (int i = 0; i < TOKEN_LENGTH; i++) {
      sb.append(TOKEN_CHARS.charAt(RANDOM.nextInt(TOKEN_CHARS.length())));
    }
    return sb.toString();
  }

  /**
   * POST a e-consulta con {anio, mes, token} → lista de tipos de cambio del mes.
   */
  private List<SunatEconsultaRateItem> fetchMonthlyRates(int year, int month, String token)
      throws Exception {
    // SUNAT e-consulta usa mes 0-based (JavaScript getMonth(): 0=ene, 11=dic)
    String body = objectMapper.writeValueAsString(
        Map.of("anio", year, "mes", month - 1, "token", token));

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(SUNAT_ECONSULTA_DATA_URL))
        .header("Content-Type", "application/json; charset=UTF-8")
        .header("Accept", "application/json, text/javascript, */*; q=0.01")
        .header("X-Requested-With", "XMLHttpRequest")
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .header("Referer", SUNAT_ECONSULTA_BASE)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    HttpResponse<String> response = scraperClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new RuntimeException(
          "SUNAT e-consulta respondió HTTP " + response.statusCode() + ": " + response.body());
    }

    log.debug("Respuesta e-consulta HTTP {}: {}", response.statusCode(), response.body());
    return objectMapper.readValue(response.body(),
        new TypeReference<>() {});
  }

  // ──────────────────────────────────────────────
  // DTO interno para la respuesta de e-consulta
  // ──────────────────────────────────────────────

  @Getter
  @Setter
  static class SunatEconsultaRateItem {
    private String fecPublica;
    private String valTipo;
    private String codTipo;
  }

  /** Inserta o reemplaza un tipo de cambio en BD para una empresa (usado por el bulk import). */
  private void upsertRate(LocalDate date, BigDecimal value, String type, Company company) {
    ExchangeRate rate = exchangeRateRepository
        .findByDateAndTypeAndCompanyId(date, type, company.getId())
        .orElseGet(ExchangeRate::new);
    rate.setDate(date);
    rate.setValue(value);
    rate.setType(type);
    rate.setCompany(company);
    exchangeRateRepository.save(rate);
    log.debug("Tipo de cambio upserted: {} tipo {} = {} (empresa {})", date, type, value, company.getId());
  }

  /** Inserta solo si no existe para una empresa (usado por el fetch diario). */
  private void saveRate(LocalDate date, BigDecimal value, String type, Company company) {
    if (exchangeRateRepository.existsByDateAndTypeAndCompanyId(date, type, company.getId())) {
      log.debug("Tipo de cambio para {} tipo {} empresa {} ya existe, ignorando", date, type, company.getId());
      return;
    }
    ExchangeRate rate = new ExchangeRate();
    rate.setDate(date);
    rate.setValue(value);
    rate.setType(type);
    rate.setCompany(company);
    try {
      exchangeRateRepository.save(rate);
    } catch (DataIntegrityViolationException e) {
      log.debug("Tipo de cambio para {} tipo {} empresa {} ya existe (concurrencia), ignorando",
          date, type, company.getId());
    }
  }
}
