package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Client;
import com.api.multiempresa.dto.entity.ClientAddress;
import com.api.multiempresa.dto.entity.PersonType;
import com.api.multiempresa.dto.filter.ClientFilter;
import com.api.multiempresa.dto.mapper.ClientAddressMapper;
import com.api.multiempresa.dto.mapper.ClientMapper;
import com.api.multiempresa.dto.request.ClientAddressRequest;
import com.api.multiempresa.dto.request.ClientRequest;
import com.api.multiempresa.dto.request.ClientStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ClientAddressResponse;
import com.api.multiempresa.dto.response.ClientResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.ClientAddressRepository;
import com.api.multiempresa.repository.ClientRepository;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.DocumentTypeRepository;
import com.api.multiempresa.repository.PersonTypeRepository;
import com.api.multiempresa.repository.spec.ClientSpecification;
import com.api.multiempresa.service.ClientService;
import com.api.multiempresa.util.ClientValidator;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

  private final ClientRepository repository;
  private final ClientAddressRepository addressRepository;
  private final PersonTypeRepository personTypeRepository;
  private final DocumentTypeRepository documentTypeRepository;
  private final CompanyRepository companyRepository;
  private final ClientValidator validator;
  private final ClientMapper mapper;
  private final ClientAddressMapper addressMapper;

  @Override
  @Transactional
  public ApiResponse<List<ClientResponse>> findAll(ClientFilter filter) {
    return new ApiResponse<>("Clientes listados correctamente",
        mapper.toResponseList(repository.findAll(ClientSpecification.byFilter(filter))));
  }

  @Override
  @Transactional
  public ApiResponse<ClientResponse> findById(Long id) {
    Client client = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    return new ApiResponse<>("Cliente obtenido correctamente", mapper.toResponse(client));
  }

  @Override
  @Transactional
  public ApiResponse<ClientResponse> create(ClientRequest request) {
    Long companyId = TenantContext.getCurrentCompanyId();
    String username = JwtUtils.extractUsernameFromContext();

    PersonType personType = personTypeRepository.findById(request.getPersonTypeId())
        .orElseThrow(() -> new ResourceNotFoundException("Tipo de persona no válido"));

    validator.validateByPersonType(personType, request);

    if (repository.existsByDocumentTypeIdAndDocumentNumberAndCompanyIdAndDeletedAtIsNull(
        request.getDocumentTypeId(), request.getDocumentNumber(), companyId)) {
      throw new BusinessValidationException(
          "Ya existe un cliente con ese tipo y número de documento");
    }

    Client client = mapper.toEntity(request);
    client.setCompany(companyRepository.getReferenceById(companyId));
    client.setPersonType(personType);
    client.setDocumentType(documentTypeRepository.findById(request.getDocumentTypeId())
        .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no válido")));
    client.setRetentionAgent(Boolean.TRUE.equals(request.getRetentionAgent()));
    client.setStatus(1);
    client.setCreatedBy(username);

    Client saved = repository.save(client);

    if (request.getAddresses() != null) {
      for (ClientAddressRequest addrReq : request.getAddresses()) {
        ClientAddress addr = new ClientAddress();
        addr.setClient(saved);
        addr.setAddress(addrReq.getAddress());
        addr.setUbigeo(addrReq.getUbigeo());
        addr.setDescription(addrReq.getDescription());
        addr.setCreatedBy(username);
        addressRepository.save(addr);
      }
    }

    ClientResponse response = mapper.toResponse(saved);
    response.setAddresses(
        addressMapper.toResponseList(addressRepository.findByClientIdAndDeletedAtIsNull(saved.getId())));
    return new ApiResponse<>("Cliente registrado correctamente", response);
  }

  @Override
  @Transactional
  public ApiResponse<ClientResponse> update(Long id, ClientRequest request) {
    String username = JwtUtils.extractUsernameFromContext();

    Client client = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

    PersonType personType = personTypeRepository.findById(request.getPersonTypeId())
        .orElseThrow(() -> new ResourceNotFoundException("Tipo de persona no válido"));

    validator.validateByPersonType(personType, request);

    Long companyId = TenantContext.getCurrentCompanyId();
    if (repository.existsByDocumentTypeIdAndDocumentNumberAndCompanyIdAndDeletedAtIsNullAndIdNot(
        request.getDocumentTypeId(), request.getDocumentNumber(), companyId, id)) {
      throw new BusinessValidationException(
          "Ya existe un cliente con ese tipo y número de documento");
    }

    mapper.updateEntity(client, request);
    client.setPersonType(personType);
    client.setDocumentType(documentTypeRepository.findById(request.getDocumentTypeId())
        .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no válido")));
    client.setUpdatedBy(username);

    Client saved = repository.save(client);

    if (request.getAddresses() != null) {
      for (ClientAddressRequest addrReq : request.getAddresses()) {
        ClientAddress addr = new ClientAddress();
        addr.setClient(saved);
        addr.setAddress(addrReq.getAddress());
        addr.setUbigeo(addrReq.getUbigeo());
        addr.setDescription(addrReq.getDescription());
        addr.setCreatedBy(username);
        addressRepository.save(addr);
      }
    }

    ClientResponse response = mapper.toResponse(saved);
    response.setAddresses(
        addressMapper.toResponseList(addressRepository.findByClientIdAndDeletedAtIsNull(id)));
    return new ApiResponse<>("Cliente actualizado correctamente", response);
  }

  @Override
  @Transactional
  public ApiResponse<Void> updateStatus(Long id, ClientStatusRequest request) {
    Client client = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

    client.setStatus(request.getStatus());
    client.setUpdatedBy(JwtUtils.extractUsernameFromContext());
    repository.save(client);

    return new ApiResponse<>("Estado del cliente actualizado correctamente", null);
  }

  // ─── Address management ──────────────────────────────────────────────────

  @Override
  public ApiResponse<List<ClientAddressResponse>> findAddresses(Long clientId) {
    if (!repository.existsById(clientId)) {
      throw new ResourceNotFoundException("Cliente no encontrado");
    }
    return new ApiResponse<>("Direcciones listadas correctamente",
        addressMapper.toResponseList(addressRepository.findByClientIdAndDeletedAtIsNull(clientId)));
  }

  @Override
  @Transactional
  public ApiResponse<ClientAddressResponse> addAddress(Long clientId,
      ClientAddressRequest request) {
    Client client = repository.findById(clientId)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

    ClientAddress addr = new ClientAddress();
    addr.setClient(client);
    addr.setAddress(request.getAddress());
    addr.setUbigeo(request.getUbigeo());
    addr.setDescription(request.getDescription());
    addr.setCreatedBy(JwtUtils.extractUsernameFromContext());

    return new ApiResponse<>("Dirección agregada correctamente",
        addressMapper.toResponse(addressRepository.save(addr)));
  }

  @Override
  @Transactional
  public ApiResponse<ClientAddressResponse> updateAddress(Long clientId, Long addressId,
      ClientAddressRequest request) {
    ClientAddress addr = addressRepository
        .findByIdAndClientIdAndDeletedAtIsNull(addressId, clientId)
        .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

    addr.setAddress(request.getAddress());
    addr.setUbigeo(request.getUbigeo());
    addr.setDescription(request.getDescription());
    addr.setUpdatedBy(JwtUtils.extractUsernameFromContext());

    return new ApiResponse<>("Dirección actualizada correctamente",
        addressMapper.toResponse(addressRepository.save(addr)));
  }

  @Override
  @Transactional
  public ApiResponse<Void> deleteAddress(Long clientId, Long addressId) {
    ClientAddress addr = addressRepository
        .findByIdAndClientIdAndDeletedAtIsNull(addressId, clientId)
        .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

    addr.setDeletedAt(LocalDateTime.now());
    addr.setDeletedBy(JwtUtils.extractUsernameFromContext());
    addressRepository.save(addr);

    return new ApiResponse<>("Dirección eliminada correctamente", null);
  }
}
