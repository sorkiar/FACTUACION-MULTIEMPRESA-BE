package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.RemissionGuideFilter;
import com.api.multiempresa.dto.request.RemissionGuideRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.RemissionGuideResponse;
import com.api.multiempresa.service.RemissionGuideService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/remission-guides")
@RequiredArgsConstructor
public class RemissionGuideController {

  private final RemissionGuideService service;

  @GetMapping
  public ApiResponse<List<RemissionGuideResponse>> list(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String series,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long transferReasonId,
      @RequestParam(required = false) String transportMode,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    RemissionGuideFilter filter = new RemissionGuideFilter();
    filter.setId(id);
    filter.setSeries(series);
    filter.setStatus(status);
    filter.setTransferReasonId(transferReasonId);
    filter.setTransportMode(transportMode);
    filter.setStartDate(startDate);
    filter.setEndDate(endDate);
    return service.findAll(filter);
  }

  @GetMapping("/{id}")
  public ApiResponse<RemissionGuideResponse> findById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ApiResponse<RemissionGuideResponse> create(
      @RequestBody @Valid RemissionGuideRequest request
  ) {
    return service.create(request);
  }
}
