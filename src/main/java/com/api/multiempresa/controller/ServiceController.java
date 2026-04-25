package com.api.multiempresa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.multiempresa.dto.filter.ServiceFilter;
import com.api.multiempresa.dto.request.ServiceRequest;
import com.api.multiempresa.dto.request.ServiceStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ServiceResponse;
import com.api.multiempresa.service.ServiceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Validated
public class ServiceController {
  private final ServiceService service;

  @GetMapping
  public ApiResponse<List<ServiceResponse>> list(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long serviceCategoryId,
      @RequestParam(required = false) String sku,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String currency
  ) {
    ServiceFilter filter = new ServiceFilter();
    filter.setId(id);
    filter.setStatus(status);
    filter.setServiceCategoryId(serviceCategoryId);
    filter.setSku(sku);
    filter.setName(name);
    filter.setCurrencyCode(currency);

    return service.findAll(filter);
  }

  @PostMapping(consumes = "multipart/form-data")
  public ApiResponse<ServiceResponse> create(
      @RequestPart("data") String data,
      @RequestPart(value = "image", required = false) MultipartFile image,
      @RequestPart(value = "technicalSheet", required = false) MultipartFile technicalSheet
  ) throws Exception {
    ServiceRequest request = new ObjectMapper().readValue(data, ServiceRequest.class);
    return service.create(request, image, technicalSheet);
  }

  @PutMapping(value = "/{id}", consumes = "multipart/form-data")
  public ApiResponse<ServiceResponse> update(
      @PathVariable Long id,
      @RequestPart("data") String data,
      @RequestPart(value = "image", required = false) MultipartFile image,
      @RequestPart(value = "technicalSheet", required = false) MultipartFile technicalSheet
  ) throws Exception {
    ServiceRequest request = new ObjectMapper().readValue(data, ServiceRequest.class);
    return service.update(id, request, image, technicalSheet);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody ServiceStatusRequest request
  ) {
    return service.updateStatus(id, request);
  }

  @GetMapping("/{id}/pdf")
  public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
    byte[] pdf = service.generatePdf(id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"servicio_" + id + ".pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }
}
