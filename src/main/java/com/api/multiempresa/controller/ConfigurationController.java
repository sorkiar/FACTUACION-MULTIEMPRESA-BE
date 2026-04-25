package com.api.multiempresa.controller;

import com.api.multiempresa.dto.request.ConfigurationRequest;
import com.api.multiempresa.dto.request.ConfigurationUpdateRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ConfigurationResponse;
import com.api.multiempresa.service.ConfigurationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configurations")
@RequiredArgsConstructor
public class ConfigurationController {

  private final ConfigurationService service;

  @GetMapping
  public ResponseEntity<ApiResponse<List<ConfigurationResponse>>> findEditable() {
    return ResponseEntity.ok(service.findEditable());
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ConfigurationResponse>> create(
      @Valid @RequestBody ConfigurationRequest request) {
    return ResponseEntity.ok(service.create(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ConfigurationResponse>> update(
      @PathVariable Long id,
      @Valid @RequestBody ConfigurationUpdateRequest request) {
    return ResponseEntity.ok(service.update(id, request));
  }
}
