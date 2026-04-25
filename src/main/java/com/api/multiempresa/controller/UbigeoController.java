package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.UbigeoFilter;
import com.api.multiempresa.dto.request.UbigeoRequest;
import com.api.multiempresa.dto.request.UbigeoStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UbigeoResponse;
import com.api.multiempresa.service.UbigeoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ubigeos")
@RequiredArgsConstructor
@Validated
public class UbigeoController {

  private final UbigeoService service;

  @GetMapping
  public ApiResponse<List<UbigeoResponse>> list(
      @RequestParam(required = false) Integer status
  ) {
    UbigeoFilter filter = new UbigeoFilter();
    filter.setStatus(status);
    return service.findAll(filter);
  }

  @GetMapping("/{ubigeo}")
  public ApiResponse<UbigeoResponse> findById(@PathVariable String ubigeo) {
    return service.findById(ubigeo);
  }

  @PostMapping
  public ApiResponse<UbigeoResponse> create(
      @Valid @RequestBody UbigeoRequest request
  ) {
    return service.create(request);
  }

  @PutMapping("/{ubigeo}")
  public ApiResponse<UbigeoResponse> update(
      @PathVariable String ubigeo,
      @Valid @RequestBody UbigeoRequest request
  ) {
    return service.update(ubigeo, request);
  }

  @PatchMapping("/{ubigeo}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable String ubigeo,
      @Valid @RequestBody UbigeoStatusRequest request
  ) {
    return service.updateStatus(ubigeo, request);
  }
}
