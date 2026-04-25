package com.api.multiempresa.controller;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DocumentSeriesResponse;
import com.api.multiempresa.service.DocumentSeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document-series")
@RequiredArgsConstructor
public class DocumentSeriesController {

  private final DocumentSeriesService service;

  @GetMapping("/next-sequence")
  public ApiResponse<DocumentSeriesResponse> getNextSequence(
      @RequestParam(required = false) String documentTypeCode,
      @RequestParam(required = false) Long seriesId
  ) {
    if (seriesId != null) {
      return service.getNextSequenceById(seriesId);
    }
    return service.getNextSequencePreview(documentTypeCode);
  }
}
