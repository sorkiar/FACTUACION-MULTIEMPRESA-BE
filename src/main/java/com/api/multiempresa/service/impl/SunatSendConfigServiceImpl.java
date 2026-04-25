package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Configuration;
import com.api.multiempresa.dto.request.SunatSendConfigRequest;
import com.api.multiempresa.dto.request.SunatSendConfigRequest.DocumentSendConfigInput;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.SunatSendConfigResponse;
import com.api.multiempresa.dto.response.SunatSendConfigResponse.DocumentSendConfig;
import com.api.multiempresa.repository.ConfigurationRepository;
import com.api.multiempresa.service.SunatSendConfigService;
import com.api.multiempresa.util.JwtUtils;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SunatSendConfigServiceImpl implements SunatSendConfigService {

  private static final String GROUP = "sunat_envio";

  private final ConfigurationRepository configurationRepository;

  // -------------------------------------------------------
  // Lectura
  // -------------------------------------------------------

  @Override
  public ApiResponse<SunatSendConfigResponse> getConfig() {
    Map<String, String> cfg = loadGroup();
    return new ApiResponse<>("Configuración de envío obtenida correctamente", buildResponse(cfg));
  }

  @Override
  public boolean isOnlineMode(String docTypeKey) {
    String value = configurationRepository
        .findByConfigGroupAndConfigKeyAndDeletedAtIsNull(GROUP, docTypeKey + "_modo")
        .map(Configuration::getConfigValue)
        .orElse("ONLINE");
    return "ONLINE".equals(value);
  }

  @Override
  public int getIntervalMinutes(String docTypeKey) {
    String value = configurationRepository
        .findByConfigGroupAndConfigKeyAndDeletedAtIsNull(
            GROUP, docTypeKey + "_intervalo_minutos")
        .map(Configuration::getConfigValue)
        .orElse("30");
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return 30;
    }
  }

  // -------------------------------------------------------
  // Escritura
  // -------------------------------------------------------

  @Override
  @Transactional
  public ApiResponse<SunatSendConfigResponse> updateConfig(SunatSendConfigRequest request) {
    String username = JwtUtils.extractUsernameFromContext();

    applyInput("boleta",         request.getBoleta(),        username);
    applyInput("factura",        request.getFactura(),       username);
    applyInput("nota_credito",   request.getNotaCredito(),   username);
    applyInput("nota_debito",    request.getNotaDebito(),    username);
    applyInput("guia_remision",  request.getGuiaRemision(),  username);

    return new ApiResponse<>("Configuración de envío actualizada correctamente",
        buildResponse(loadGroup()));
  }

  // -------------------------------------------------------
  // Helpers privados
  // -------------------------------------------------------

  private Map<String, String> loadGroup() {
    return configurationRepository
        .findByConfigGroupAndDeletedAtIsNull(GROUP)
        .stream()
        .collect(Collectors.toMap(Configuration::getConfigKey, Configuration::getConfigValue));
  }

  private SunatSendConfigResponse buildResponse(Map<String, String> cfg) {
    SunatSendConfigResponse res = new SunatSendConfigResponse();
    res.setBoleta(       toDocConfig(cfg, "boleta"));
    res.setFactura(      toDocConfig(cfg, "factura"));
    res.setNotaCredito(  toDocConfig(cfg, "nota_credito"));
    res.setNotaDebito(   toDocConfig(cfg, "nota_debito"));
    res.setGuiaRemision( toDocConfig(cfg, "guia_remision"));
    return res;
  }

  private DocumentSendConfig toDocConfig(Map<String, String> cfg, String prefix) {
    String modo = cfg.getOrDefault(prefix + "_modo", "ONLINE");
    int intervalo;
    try {
      intervalo = Integer.parseInt(cfg.getOrDefault(prefix + "_intervalo_minutos", "30"));
    } catch (NumberFormatException e) {
      intervalo = 30;
    }
    return new DocumentSendConfig(modo, intervalo);
  }

  private void applyInput(String prefix, DocumentSendConfigInput input, String username) {
    if (input == null) return;
    if (input.getModo() != null) {
      saveOrUpdate(prefix + "_modo", input.getModo(), "STRING", username);
    }
    if (input.getIntervaloMinutos() != null) {
      saveOrUpdate(prefix + "_intervalo_minutos",
          String.valueOf(input.getIntervaloMinutos()), "INTEGER", username);
    }
  }

  private void saveOrUpdate(String key, String value, String datatype, String username) {
    Configuration cfg = configurationRepository
        .findByConfigGroupAndConfigKeyAndDeletedAtIsNull(GROUP, key)
        .orElseGet(() -> {
          Configuration c = new Configuration();
          c.setConfigGroup(GROUP);
          c.setConfigKey(key);
          c.setConfigDatatype(datatype);
          c.setCreatedBy(username);
          return c;
        });
    cfg.setConfigValue(value);
    cfg.setUpdatedBy(username);
    configurationRepository.save(cfg);
  }
}
