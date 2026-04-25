package com.api.multiempresa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.multiempresa.dto.filter.ProductFilter;
import com.api.multiempresa.dto.request.ProductRequest;
import com.api.multiempresa.dto.request.ProductStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ProductResponse;
import com.api.multiempresa.service.ProductService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

  private final ProductService service;

  @GetMapping
  public ApiResponse<List<ProductResponse>> list(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String sku,
      @RequestParam(required = false) String currency
  ) {
    ProductFilter filter = new ProductFilter();
    filter.setId(id);
    filter.setStatus(status);
    filter.setCategoryId(categoryId);
    filter.setSku(sku);
    filter.setCurrencyCode(currency);

    return service.findAll(filter);
  }

  @PostMapping(consumes = "multipart/form-data")
  public ApiResponse<ProductResponse> create(
      @RequestPart("data") String data,
      @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
      @RequestPart(value = "technicalSheet", required = false) MultipartFile technicalSheet
  ) throws Exception {
    ProductRequest request = new ObjectMapper().readValue(data, ProductRequest.class);
    return service.create(request, mainImage, technicalSheet);
  }

  @PutMapping(value = "/{id}", consumes = "multipart/form-data")
  public ApiResponse<ProductResponse> update(
      @PathVariable Long id,
      @RequestPart("data") String data,
      @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
      @RequestPart(value = "technicalSheet", required = false) MultipartFile technicalSheet
  ) throws Exception {
    ProductRequest request = new ObjectMapper().readValue(data, ProductRequest.class);
    return service.update(id, request, mainImage, technicalSheet);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody ProductStatusRequest request
  ) {
    return service.updateStatus(id, request);
  }
}
