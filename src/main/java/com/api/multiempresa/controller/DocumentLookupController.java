package com.api.multiempresa.controller;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DniRecordResponse;
import com.api.multiempresa.dto.response.RucRecordResponse;
import com.api.multiempresa.service.DocumentLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document-lookup")
@RequiredArgsConstructor
public class DocumentLookupController {

  private final DocumentLookupService documentLookupService;

  @GetMapping("/dni/{dni}")
  public ApiResponse<DniRecordResponse> queryDni(@PathVariable String dni) {
    return documentLookupService.queryDni(dni);
  }

  @GetMapping("/ruc/{ruc}")
  public ApiResponse<RucRecordResponse> queryRuc(@PathVariable String ruc) {
    return documentLookupService.queryRuc(ruc);
  }
}
