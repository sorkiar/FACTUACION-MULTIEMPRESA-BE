package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.ServiceCategoryFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ServiceCategoryResponse;
import com.api.multiempresa.service.ServiceCategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-categories")
@RequiredArgsConstructor
public class ServiceCategoryController {
  private final ServiceCategoryService service;

  @GetMapping
  public ApiResponse<List<ServiceCategoryResponse>> list(
      @RequestParam(required = false) Integer status
  ) {
    ServiceCategoryFilter filter = new ServiceCategoryFilter();
    filter.setStatus(status);

    return service.findAll(filter);
  }
}
