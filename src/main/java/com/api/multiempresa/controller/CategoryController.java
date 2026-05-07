package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.CategoryFilter;
import com.api.multiempresa.dto.request.CategoryRequest;
import com.api.multiempresa.dto.request.CategoryStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CategoryResponse;
import com.api.multiempresa.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

  private final CategoryService service;

  @GetMapping
  public ApiResponse<List<CategoryResponse>> list(
      @RequestParam(required = false) Integer status) {
    CategoryFilter filter = new CategoryFilter();
    filter.setStatus(status);
    return service.findAll(filter);
  }

  @GetMapping("/{id}")
  public ApiResponse<CategoryResponse> findById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public ApiResponse<CategoryResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody CategoryRequest request) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody CategoryStatusRequest request) {
    return service.updateStatus(id, request);
  }
}
