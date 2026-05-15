package com.api.multiempresa.controller;

import com.api.multiempresa.dto.request.DocumentSeriesCreateRequest;
import com.api.multiempresa.dto.request.DocumentSeriesUpdateRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DocumentSeriesResponse;
import com.api.multiempresa.service.DocumentSeriesService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/document-series")
@RequiredArgsConstructor
public class DocumentSeriesController {

  private final DocumentSeriesService service;

  @GetMapping
  public ApiResponse<List<DocumentSeriesResponse>> findAll() {
    return service.findAll();
  }

  @PostMapping
  public ApiResponse<DocumentSeriesResponse> create(
      @RequestBody @Valid DocumentSeriesCreateRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public ApiResponse<DocumentSeriesResponse> update(
      @PathVariable Long id,
      @RequestBody @Valid DocumentSeriesUpdateRequest request) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<DocumentSeriesResponse> toggleStatus(@PathVariable Long id) {
    return service.toggleStatus(id);
  }

  @GetMapping("/next-sequence")
  public ApiResponse<DocumentSeriesResponse> getNextSequence(
      @RequestParam(required = false) String documentTypeCode,
      @RequestParam(required = false) Long seriesId) {
    if (seriesId != null) {
      return service.getNextSequenceById(seriesId);
    }
    return service.getNextSequencePreview(documentTypeCode);
  }
}
