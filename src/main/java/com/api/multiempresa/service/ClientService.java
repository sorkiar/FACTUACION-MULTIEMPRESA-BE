package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.ClientFilter;
import com.api.multiempresa.dto.request.ClientAddressRequest;
import com.api.multiempresa.dto.request.ClientRequest;
import com.api.multiempresa.dto.request.ClientStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ClientAddressResponse;
import com.api.multiempresa.dto.response.ClientResponse;
import java.util.List;

public interface ClientService {

  ApiResponse<List<ClientResponse>> findAll(ClientFilter filter);

  ApiResponse<ClientResponse> findById(Long id);

  ApiResponse<ClientResponse> create(ClientRequest request);

  ApiResponse<ClientResponse> update(Long id, ClientRequest request);

  ApiResponse<Void> updateStatus(Long id, ClientStatusRequest request);

  // Address management
  ApiResponse<List<ClientAddressResponse>> findAddresses(Long clientId);

  ApiResponse<ClientAddressResponse> addAddress(Long clientId, ClientAddressRequest request);

  ApiResponse<ClientAddressResponse> updateAddress(Long clientId, Long addressId,
      ClientAddressRequest request);

  ApiResponse<Void> deleteAddress(Long clientId, Long addressId);
}
