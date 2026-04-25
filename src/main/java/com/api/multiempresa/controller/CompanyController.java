package com.api.multiempresa.controller;

import com.api.multiempresa.dto.request.CompanyRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CompanyResponse;
import com.api.multiempresa.service.CompanyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CompanyController {

  private final CompanyService companyService;

  @PostMapping
  public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CompanyRequest request) {
    return ResponseEntity.ok(companyService.create(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CompanyResponse>> update(@PathVariable Long id,
      @Valid @RequestBody CompanyRequest request) {
    return ResponseEntity.ok(companyService.update(id, request));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CompanyResponse>> findById(@PathVariable Long id) {
    return ResponseEntity.ok(companyService.findById(id));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<CompanyResponse>>> findAll() {
    return ResponseEntity.ok(companyService.findAll());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    return ResponseEntity.ok(companyService.delete(id));
  }
}
