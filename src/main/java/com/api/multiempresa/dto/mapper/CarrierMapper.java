package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Carrier;
import com.api.multiempresa.dto.response.CarrierResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarrierMapper {

  CarrierResponse toResponse(Carrier entity);

  List<CarrierResponse> toResponseList(List<Carrier> entities);
}
