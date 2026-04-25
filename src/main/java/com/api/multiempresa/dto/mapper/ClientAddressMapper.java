package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.ClientAddress;
import com.api.multiempresa.dto.response.ClientAddressResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientAddressMapper {

  ClientAddressResponse toResponse(ClientAddress entity);

  List<ClientAddressResponse> toResponseList(List<ClientAddress> entities);
}
