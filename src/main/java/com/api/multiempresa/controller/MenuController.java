package com.api.multiempresa.controller;

import com.api.multiempresa.dto.request.MenuRequest;
import com.api.multiempresa.dto.request.MenuStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.MenuResponse;
import com.api.multiempresa.dto.response.SidebarItemResponse;
import com.api.multiempresa.service.MenuService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Validated
public class MenuController {

  private final MenuService service;

  @GetMapping
  public ApiResponse<List<MenuResponse>> list() {
    return service.findAll();
  }

  @GetMapping("/sidebar")
  public ApiResponse<List<SidebarItemResponse>> sidebar() {
    return service.getSidebar();
  }

  @GetMapping("/navbar")
  public ApiResponse<List<SidebarItemResponse>> navbar() {
    return service.getNavbar();
  }

  @GetMapping("/internal")
  public ApiResponse<List<SidebarItemResponse>> internal() {
    return service.getInternal();
  }

  @GetMapping("/allowed-paths")
  public ApiResponse<List<String>> allowedPaths() {
    return service.getAllowedPaths();
  }

  @GetMapping("/{id}")
  public ApiResponse<MenuResponse> findById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ApiResponse<MenuResponse> create(@Valid @RequestBody MenuRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public ApiResponse<MenuResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody MenuRequest request
  ) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody MenuStatusRequest request
  ) {
    return service.updateStatus(id, request);
  }
}
