package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.ProfileFilter;
import com.api.multiempresa.dto.request.ProfileMenuRequest;
import com.api.multiempresa.dto.request.ProfileRequest;
import com.api.multiempresa.dto.request.ProfileStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.MenuResponse;
import com.api.multiempresa.dto.response.ProfileResponse;
import com.api.multiempresa.service.ProfileService;
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
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Validated
public class ProfileController {

  private final ProfileService service;

  @GetMapping
  public ApiResponse<List<ProfileResponse>> list(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Integer status
  ) {
    ProfileFilter filter = new ProfileFilter();
    filter.setName(name);
    filter.setStatus(status);
    return service.findAll(filter);
  }

  @GetMapping("/{id}")
  public ApiResponse<ProfileResponse> findById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ApiResponse<ProfileResponse> create(@Valid @RequestBody ProfileRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public ApiResponse<ProfileResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody ProfileRequest request
  ) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody ProfileStatusRequest request
  ) {
    return service.updateStatus(id, request);
  }

  @GetMapping("/{id}/menus")
  public ApiResponse<List<MenuResponse>> getMenus(@PathVariable Long id) {
    return service.getMenus(id);
  }

  @PutMapping("/{id}/menus")
  public ApiResponse<Void> updateMenus(
      @PathVariable Long id,
      @Valid @RequestBody ProfileMenuRequest request
  ) {
    return service.updateMenus(id, request);
  }
}
