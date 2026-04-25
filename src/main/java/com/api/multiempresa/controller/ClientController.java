package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.ClientFilter;
import com.api.multiempresa.dto.request.ClientAddressRequest;
import com.api.multiempresa.dto.request.ClientRequest;
import com.api.multiempresa.dto.request.ClientStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ClientAddressResponse;
import com.api.multiempresa.dto.response.ClientResponse;
import com.api.multiempresa.service.ClientService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Validated
public class ClientController {

  private final ClientService service;

  @GetMapping
  public ApiResponse<List<ClientResponse>> list(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long documentTypeId,
      @RequestParam(required = false) String documentNumber
  ) {
    ClientFilter filter = new ClientFilter();
    filter.setId(id);
    filter.setStatus(status);
    filter.setDocumentTypeId(documentTypeId);
    filter.setDocumentNumber(documentNumber);
    return service.findAll(filter);
  }

  @GetMapping("/{id}")
  public ApiResponse<ClientResponse> findById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ApiResponse<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public ApiResponse<ClientResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody ClientRequest request
  ) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody ClientStatusRequest request
  ) {
    return service.updateStatus(id, request);
  }

  // ─── Address endpoints ───────────────────────────────────────────────────

  @GetMapping("/{clientId}/addresses")
  public ApiResponse<List<ClientAddressResponse>> listAddresses(@PathVariable Long clientId) {
    return service.findAddresses(clientId);
  }

  @PostMapping("/{clientId}/addresses")
  public ApiResponse<ClientAddressResponse> addAddress(
      @PathVariable Long clientId,
      @Valid @RequestBody ClientAddressRequest request
  ) {
    return service.addAddress(clientId, request);
  }

  @PutMapping("/{clientId}/addresses/{addressId}")
  public ApiResponse<ClientAddressResponse> updateAddress(
      @PathVariable Long clientId,
      @PathVariable Long addressId,
      @Valid @RequestBody ClientAddressRequest request
  ) {
    return service.updateAddress(clientId, addressId, request);
  }

  @DeleteMapping("/{clientId}/addresses/{addressId}")
  public ApiResponse<Void> deleteAddress(
      @PathVariable Long clientId,
      @PathVariable Long addressId
  ) {
    return service.deleteAddress(clientId, addressId);
  }
}
