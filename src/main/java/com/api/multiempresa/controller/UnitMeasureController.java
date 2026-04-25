package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.UnitMeasureFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UnitMeasureResponse;
import com.api.multiempresa.service.UnitMeasureService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unit-measures")
@RequiredArgsConstructor
public class UnitMeasureController {
  private final UnitMeasureService service;

  @GetMapping
  public ApiResponse<List<UnitMeasureResponse>> list(
      @RequestParam(required = false) Integer status
  ) {
    UnitMeasureFilter filter = new UnitMeasureFilter();
    filter.setStatus(status);

    return service.findAll(filter);
  }
}
