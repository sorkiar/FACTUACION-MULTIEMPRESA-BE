package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.CategoryFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CategoryResponse;
import com.api.multiempresa.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService service;

  @GetMapping
  public ApiResponse<List<CategoryResponse>> list(
      @RequestParam(required = false) Integer status
  ) {
    CategoryFilter filter = new CategoryFilter();
    filter.setStatus(status);

    return service.findAll(filter);
  }
}
