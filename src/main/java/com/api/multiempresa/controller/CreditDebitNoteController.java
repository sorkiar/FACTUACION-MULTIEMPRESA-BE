package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.CreditDebitNoteFilter;
import com.api.multiempresa.dto.request.CreditDebitNoteRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CreditDebitNoteResponse;
import com.api.multiempresa.service.CreditDebitNoteService;
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
@RequestMapping("/api/credit-debit-notes")
@RequiredArgsConstructor
public class CreditDebitNoteController {

  private final CreditDebitNoteService service;

  @GetMapping
  public ApiResponse<List<CreditDebitNoteResponse>> list(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long saleId,
      @RequestParam(required = false) String documentTypeCode,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    CreditDebitNoteFilter filter = new CreditDebitNoteFilter();
    filter.setId(id);
    filter.setSaleId(saleId);
    filter.setDocumentTypeCode(documentTypeCode);
    filter.setStatus(status);
    filter.setStartDate(startDate);
    filter.setEndDate(endDate);
    return service.findAll(filter);
  }

  @GetMapping("/{id}")
  public ApiResponse<CreditDebitNoteResponse> findById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ApiResponse<CreditDebitNoteResponse> create(
      @RequestBody @Valid CreditDebitNoteRequest request
  ) {
    return service.create(request);
  }
}
