package com.api.multiempresa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.multiempresa.dto.filter.SaleFilter;
import com.api.multiempresa.dto.request.SaleRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.SaleResponse;
import com.api.multiempresa.service.SaleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Validated
public class SaleController {

  private final SaleService service;

  @GetMapping
  public ApiResponse<List<SaleResponse>> list(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long clientId,
      @RequestParam(required = false) String saleStatus,
      @RequestParam(required = false) String paymentStatus,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false) Boolean excludeAnnulled
  ) {

    SaleFilter filter = new SaleFilter();
    filter.setId(id);
    filter.setClientId(clientId);
    filter.setSaleStatus(saleStatus);
    filter.setPaymentStatus(paymentStatus);
    filter.setStartDate(startDate);
    filter.setEndDate(endDate);
    filter.setExcludeAnnulled(excludeAnnulled);

    return service.findAll(filter);
  }

  @PostMapping(consumes = "multipart/form-data")
  public ApiResponse<SaleResponse> create(
      @RequestPart("data") String data,
      @RequestParam(required = false)
      MultiValueMap<String, MultipartFile> paymentProofs
  ) throws Exception {
    SaleRequest request = new ObjectMapper().readValue(data, SaleRequest.class);
    return service.create(request, paymentProofs);
  }

  @PutMapping(value = "/{id}/draft", consumes = "multipart/form-data")
  public ApiResponse<SaleResponse> updateDraft(
      @PathVariable Long id,
      @RequestPart("data") String data,
      @RequestParam(required = false)
      MultiValueMap<String, MultipartFile> paymentProofs
  ) throws Exception {
    SaleRequest request = new ObjectMapper().readValue(data, SaleRequest.class);
    return service.updateDraft(id, request, paymentProofs);
  }

  @PostMapping("/quotation")
  public ResponseEntity<byte[]> generateQuotation(
      @Valid @RequestBody SaleRequest request
  ) {
    byte[] pdf = service.generateQuotation(request);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cotizacion.pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }
}
