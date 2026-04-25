package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.CreditDebitNoteTypeFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CreditDebitNoteTypeResponse;
import com.api.multiempresa.service.CreditDebitNoteTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credit-debit-note-types")
@RequiredArgsConstructor
public class CreditDebitNoteTypeController {

  private final CreditDebitNoteTypeService service;

  @GetMapping
  public ApiResponse<List<CreditDebitNoteTypeResponse>> list(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String noteCategory,
      @RequestParam(required = false) Integer status
  ) {
    CreditDebitNoteTypeFilter filter = new CreditDebitNoteTypeFilter();
    filter.setCode(code);
    filter.setNoteCategory(noteCategory);
    filter.setStatus(status);
    return service.findAll(filter);
  }
}
