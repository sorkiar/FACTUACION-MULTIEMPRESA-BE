package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Driver;
import com.api.multiempresa.dto.entity.DriverVehicle;
import com.api.multiempresa.dto.response.DriverResponse;
import com.api.multiempresa.dto.response.DriverVehicleResponse;
import java.util.List;
import java.util.Set;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DriverMapper {

  /** Mapea sin incluir la lista de vehículos (para listas y contextos embebidos). */
  @Named("toDriverResponse")
  @Mapping(target = "vehicles", ignore = true)
  DriverResponse toResponse(Driver entity);

  /** Mapea incluyendo la lista de vehículos activos (para findById). */
  @Named("toDriverResponseWithVehicles")
  @Mapping(target = "vehicles", source = "vehicles")
  DriverResponse toResponseWithVehicles(Driver entity);

  DriverVehicleResponse toVehicleResponse(DriverVehicle entity);

  @IterableMapping(qualifiedByName = "toDriverResponse")
  List<DriverResponse> toResponseList(List<Driver> entities);

  List<DriverVehicleResponse> toVehicleResponseList(List<DriverVehicle> entities);

  default List<DriverVehicleResponse> mapVehicles(Set<DriverVehicle> vehicles) {
    if (vehicles == null) return null;
    return vehicles.stream()
        .filter(v -> v.getDeletedAt() == null)
        .map(this::toVehicleResponse)
        .toList();
  }
}
