package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Client;
import com.api.multiempresa.dto.request.ClientRequest;
import com.api.multiempresa.dto.response.ClientResponse;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {ClientAddressMapper.class})
public interface ClientMapper {
  /* ============================
     CREATE
     ============================ */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "personType", ignore = true)
  @Mapping(target = "documentType", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "addresses", ignore = true)
  @Mapping(target = "birthDate", source = "birthDate", dateFormat = "yyyy-MM-dd")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deletedBy", ignore = true)
  Client toEntity(ClientRequest request);

  /* ============================
     UPDATE (PUT)
     ============================ */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "personType", ignore = true)
  @Mapping(target = "documentType", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "addresses", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deletedBy", ignore = true)
  void updateEntity(@MappingTarget Client entity, ClientRequest request);

  /* ============================
     RESPONSE
     ============================ */
  @Mapping(target = "personTypeId", source = "personType.id")
  @Mapping(target = "personType", source = "personType.name")
  @Mapping(target = "documentTypeId", source = "documentType.id")
  @Mapping(target = "documentType", source = "documentType.name")
  @Mapping(target = "birthDate", source = "birthDate", dateFormat = "yyyy-MM-dd")
  ClientResponse toResponse(Client client);

  List<ClientResponse> toResponseList(List<Client> clients);
}
