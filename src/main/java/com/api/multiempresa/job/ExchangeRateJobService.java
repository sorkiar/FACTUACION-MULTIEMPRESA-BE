package com.api.multiempresa.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.entity.ExchangeRate;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.ExchangeRateRepository;
import com.api.multiempresa.service.ConfigurationService;
import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateJobService {

  private static final String SUNAT_ECONSULTA_BASE =
      "https://e-consulta.sunat.gob.pe/cl-at-ittipcam/tcS01Alias";

  private static final String SUNAT_ECONSULTA_DATA_URL =
      SUNAT_ECONSULTA_BASE + "/listarTipoCambio";

  private static final String CONFIG_GROUP = "tipo_cambio";
  private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");
  private static final int FETCH_START_HOUR = 1;

  private static final DateTimeFormatter ECONSULTA_DATE_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final ExchangeRateRepository exchangeRateRepository;
  private final CompanyRepository companyRepository;
  private final ConfigurationService configurationService;
  private final ObjectMapper objectMapper;

  /** HttpClient con CookieManager para mantener sesión entre GET (token) y POST (datos). */
  private final HttpClient scraperClient = HttpClient.newBuilder()
      .cookieHandler(new CookieManager())
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build();

  /**
   * Corre cada 5 minutos. Intenta obtener el TC en ventanas horarias cuyo intervalo
   * se lee dinámicamente de la configuración (clave fetch_hour del grupo tipo_cambio).
   * Ejemplo con fetch_hour=5: intenta a la 1h, 6h, 11h, 16h, 21h.
   */
  @Scheduled(fixedRate = 300_000)
  public void scheduledFetch() {
    try {
      Map<String, String> config = configurationService.getGroup(CONFIG_GROUP);
      int intervalHours = Math.max(1, Integer.parseInt(config.getOrDefault("fetch_hour", "5")));
      int currentHour = LocalTime.now(LIMA_ZONE).getHour();

      boolean inFetchWindow = false;
      for (int h = FETCH_START_HOUR; h < 24; h += intervalHours) {
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
   * Al iniciar la aplicación intenta obtener/actualizar el tipo de cambio del día.
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
    Company first = companies.get(0);
    if (exchangeRateRepository.existsByDateAndTypeAndCompanyId(today, "C", first.getId())) {
      log.debug("TC para {} ya registrado, se omite fetch", today);
      return;
    }
    fetchAndUpsertToday(today, companies);
  }

  /**
   * Obtiene el TC del día desde e-consulta y lo persiste para todas las empresas.
   * Si SUNAT aún no publicó el TC se reintentará en la próxima ventana horaria.
   */
  private void fetchAndUpsertToday(LocalDate today, List<Company> companies) {
    try {
      String token = fetchEconsultaToken();
      List<SunatEconsultaRateItem> items =
          fetchMonthlyRates(today.getYear(), today.getMonthValue(), token);

      int saved = 0;
      for (SunatEconsultaRateItem item : items) {
        LocalDate date = LocalDate.parse(item.getFecPublica(), ECONSULTA_DATE_FORMAT);
        if (!date.equals(today)) continue;

        BigDecimal value = new BigDecimal(item.getValTipo().trim());
        String type = item.getCodTipo();
        for (Company company : companies) {
          upsertRate(date, value, type, company);
        }
        saved++;
      }

      if (saved == 0) {
        log.warn("e-consulta aún no publicó TC para {}, se reintentará en la próxima ventana horaria.", today);
      } else {
        log.info("TC del {} actualizado para {} empresa(s).", today, companies.size());
      }
    } catch (Exception e) {
      log.error("Error obteniendo TC del día desde e-consulta: {}", e.getMessage());
    }
  }

  // ──────────────────────────────────────────────
  // Importación masiva via SUNAT e-consulta (API)
  // ──────────────────────────────────────────────

  /**
   * Obtiene los tipos de cambio de SUNAT e-consulta para el rango [from, to] (mismo mes/año)
   * y los persiste para todas las empresas indicadas. Devuelve la cantidad de registros procesados.
   */
  public int fetchAndSaveRange(LocalDate from, LocalDate to, List<Company> companies) {
    log.info("Importando tipos de cambio desde SUNAT e-consulta: {} a {} para {} empresa(s)",
        from, to, companies.size());
    try {
      String token = fetchEconsultaToken();
      List<SunatEconsultaRateItem> items =
          fetchMonthlyRates(from.getYear(), from.getMonthValue(), token);

      int saved = 0;
      for (SunatEconsultaRateItem item : items) {
        LocalDate date = LocalDate.parse(item.getFecPublica(), ECONSULTA_DATE_FORMAT);
        if (date.isBefore(from) || date.isAfter(to)) continue;

        BigDecimal value = new BigDecimal(item.getValTipo().trim());
        String type = item.getCodTipo();
        for (Company company : companies) {
          upsertRate(date, value, type, company);
        }
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
   * GET a e-consulta para establecer las cookies de sesión.
   * El token reCAPTCHA v3 no es recuperable del HTML — se genera uno aleatorio con el mismo formato.
   */
  private String fetchEconsultaToken() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(SUNAT_ECONSULTA_BASE))
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .GET()
        .build();

    scraperClient.send(request, HttpResponse.BodyHandlers.ofString());

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
   * SUNAT usa mes 0-based (JavaScript getMonth(): 0=ene, 11=dic).
   */
  private List<SunatEconsultaRateItem> fetchMonthlyRates(int year, int month, String token)
      throws Exception {
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
    return objectMapper.readValue(response.body(), new TypeReference<>() {});
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

  /** Inserta o reemplaza un tipo de cambio en BD para una empresa (bulk import). */
  private void upsertRate(LocalDate date, BigDecimal value, String type, Company company) {
    ExchangeRate rate = exchangeRateRepository
        .findByDateAndTypeAndCompanyId(date, type, company.getId())
        .orElseGet(ExchangeRate::new);
    rate.setDate(date);
    rate.setValue(value);
    rate.setType(type);
    rate.setCompany(company);
    exchangeRateRepository.save(rate);
    log.debug("TC upserted: {} tipo {} = {} (empresa {})", date, type, value, company.getId());
  }

  /** Inserta solo si no existe para una empresa (fetch diario). */
  private void saveRate(LocalDate date, BigDecimal value, String type, Company company) {
    if (exchangeRateRepository.existsByDateAndTypeAndCompanyId(date, type, company.getId())) {
      log.debug("TC para {} tipo {} empresa {} ya existe, ignorando", date, type, company.getId());
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
      log.debug("TC para {} tipo {} empresa {} ya existe (concurrencia), ignorando",
          date, type, company.getId());
    }
  }
}
