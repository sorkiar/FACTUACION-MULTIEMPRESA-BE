package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Configuration;
import com.api.multiempresa.dto.request.ConfigurationRequest;
import com.api.multiempresa.dto.request.ConfigurationUpdateRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ConfigurationResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.ConfigurationRepository;
import com.api.multiempresa.service.ConfigurationService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {

  private final ConfigurationRepository repository;
  private final CompanyRepository companyRepository;

  @Override
  public Map<String, String> getGroup(String group) {
    List<Configuration> configs = repository.findByConfigGroupAndDeletedAtIsNull(group);
    Map<String, String> map = new HashMap<>();
    for (Configuration config : configs) {
      map.put(config.getConfigKey(), config.getConfigValue());
    }
    return map;
  }

  @Override
  public ApiResponse<List<ConfigurationResponse>> findEditable() {
    List<Configuration> configs = repository.findEditableOrdered(1);
    return new ApiResponse<>("Configuraciones obtenidas correctamente",
        configs.stream().map(this::toResponse).toList());
  }

  @Override
  @Transactional
  public ApiResponse<ConfigurationResponse> create(ConfigurationRequest request) {
    String username = JwtUtils.extractUsernameFromContext();
    Long companyId = TenantContext.getCompanyId();

    if (repository.findByConfigGroupAndConfigKeyAndDeletedAtIsNull(
        request.getConfigGroup(), request.getConfigKey()).isPresent()) {
      throw new BusinessValidationException(
          "Ya existe una configuración con el grupo '" + request.getConfigGroup()
              + "' y clave '" + request.getConfigKey() + "'");
    }

    Configuration config = new Configuration();
    config.setConfigGroup(request.getConfigGroup());
    config.setConfigKey(request.getConfigKey());
    config.setConfigValue(request.getConfigValue());
    config.setConfigDatatype(request.getConfigDatatype());
    config.setDescription(request.getDescription());
    config.setEditable(request.getEditable());
    config.setCreatedBy(username);
    config.setCompany(companyRepository.getReferenceById(companyId));

    return new ApiResponse<>("Configuración creada correctamente",
        toResponse(repository.save(config)));
  }

  @Override
  @Transactional
  public ApiResponse<ConfigurationResponse> update(Long id, ConfigurationUpdateRequest request) {
    String username = JwtUtils.extractUsernameFromContext();

    Configuration config = repository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada"));

    config.setConfigValue(request.getConfigValue());
    config.setDescription(request.getDescription());
    config.setEditable(request.getEditable());
    config.setUpdatedBy(username);

    return new ApiResponse<>("Configuración actualizada correctamente",
        toResponse(repository.save(config)));
  }

  private ConfigurationResponse toResponse(Configuration config) {
    ConfigurationResponse response = new ConfigurationResponse();
    response.setId(config.getId());
    response.setConfigGroup(config.getConfigGroup());
    response.setConfigKey(config.getConfigKey());
    response.setConfigValue(config.getConfigValue());
    response.setConfigDatatype(config.getConfigDatatype());
    response.setDescription(config.getDescription());
    response.setEditable(config.getEditable());
    response.setSortOrder(config.getSortOrder());
    response.setColSpan(config.getColSpan());
    response.setCreatedAt(config.getCreatedAt());
    response.setUpdatedAt(config.getUpdatedAt());
    return response;
  }
}
