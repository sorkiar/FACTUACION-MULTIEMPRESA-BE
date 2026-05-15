package com.api.multiempresa.controller;

import com.api.multiempresa.dto.request.CompanyRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CompanyResponse;
import com.api.multiempresa.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me/company")
@RequiredArgsConstructor
public class MeCompanyController {

  private final CompanyService companyService;

  @GetMapping
  public ResponseEntity<ApiResponse<CompanyResponse>> get() {
    return ResponseEntity.ok(companyService.findMyCompany());
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<CompanyResponse>> update(
      @Valid @ModelAttribute CompanyRequest request,
      @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
      @RequestPart(value = "pfxFile", required = false) MultipartFile pfxFile,
      @RequestParam(required = false) String pfxPassword) {
    return ResponseEntity.ok(
        companyService.updateMyCompany(request, logoFile, pfxFile, pfxPassword));
  }
}
