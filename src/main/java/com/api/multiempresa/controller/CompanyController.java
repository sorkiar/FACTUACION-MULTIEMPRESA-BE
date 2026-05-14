package com.api.multiempresa.controller;

import com.api.multiempresa.dto.request.CompanyRequest;
import com.api.multiempresa.dto.request.CompanyStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CompanyResponse;
import com.api.multiempresa.service.CompanyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CompanyController {

  private final CompanyService companyService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<CompanyResponse>> create(
      @Valid @ModelAttribute CompanyRequest request,
      @RequestPart(value = "pfxFile", required = false) MultipartFile pfxFile,
      @RequestParam(required = false) String pfxPassword) {
    return ResponseEntity.ok(companyService.create(request, pfxFile, pfxPassword));
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<CompanyResponse>> update(
      @PathVariable Long id,
      @Valid @ModelAttribute CompanyRequest request,
      @RequestPart(value = "pfxFile", required = false) MultipartFile pfxFile,
      @RequestParam(required = false) String pfxPassword) {
    return ResponseEntity.ok(companyService.update(id, request, pfxFile, pfxPassword));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CompanyResponse>> findById(@PathVariable Long id) {
    return ResponseEntity.ok(companyService.findById(id));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<CompanyResponse>>> findAll() {
    return ResponseEntity.ok(companyService.findAll());
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<ApiResponse<Void>> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody CompanyStatusRequest request) {
    return ResponseEntity.ok(companyService.updateStatus(id, request));
  }
}
