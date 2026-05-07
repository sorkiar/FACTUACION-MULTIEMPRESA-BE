package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.ServiceCategoryFilter;
import com.api.multiempresa.dto.request.ServiceCategoryRequest;
import com.api.multiempresa.dto.request.ServiceCategoryStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ServiceCategoryResponse;
import com.api.multiempresa.service.ServiceCategoryService;
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
@RequestMapping("/api/service-categories")
@RequiredArgsConstructor
@Validated
public class ServiceCategoryController {

  private final ServiceCategoryService service;

  @GetMapping
  public ApiResponse<List<ServiceCategoryResponse>> list(
      @RequestParam(required = false) Integer status) {
    ServiceCategoryFilter filter = new ServiceCategoryFilter();
    filter.setStatus(status);
    return service.findAll(filter);
  }

  @GetMapping("/{id}")
  public ApiResponse<ServiceCategoryResponse> findById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ApiResponse<ServiceCategoryResponse> create(
      @Valid @RequestBody ServiceCategoryRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public ApiResponse<ServiceCategoryResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody ServiceCategoryRequest request) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody ServiceCategoryStatusRequest request) {
    return service.updateStatus(id, request);
  }
}
