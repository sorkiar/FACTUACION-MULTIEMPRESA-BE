package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.DetractionCodeFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DetractionCodeResponse;
import com.api.multiempresa.service.DetractionCodeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/detraction-codes")
@RequiredArgsConstructor
public class DetractionCodeController {

  private final DetractionCodeService service;

  @GetMapping
  public ApiResponse<List<DetractionCodeResponse>> list(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Integer status
  ) {
    DetractionCodeFilter filter = new DetractionCodeFilter();
    filter.setCode(code);
    filter.setCategory(category);
    filter.setStatus(status);
    return service.findAll(filter);
  }
}
