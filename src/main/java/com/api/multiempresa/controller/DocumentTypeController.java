package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.DocumentTypeFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DocumentTypeResponse;
import com.api.multiempresa.service.DocumentTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document-types")
@RequiredArgsConstructor
public class DocumentTypeController {
  private final DocumentTypeService service;

  @GetMapping
  public ApiResponse<List<DocumentTypeResponse>> list(
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long personTypeId
  ) {
    DocumentTypeFilter filter = new DocumentTypeFilter();
    filter.setStatus(status);
    filter.setPersonTypeId(personTypeId);

    return service.findAll(filter);
  }
}
