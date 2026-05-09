package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.RemissionGuide;
import com.api.multiempresa.dto.entity.RemissionGuideDriver;
import com.api.multiempresa.dto.entity.RemissionGuideItem;
import com.api.multiempresa.dto.response.RemissionGuideDriverResponse;
import com.api.multiempresa.dto.response.RemissionGuideItemResponse;
import com.api.multiempresa.dto.response.RemissionGuideResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring",
    uses = {ClientMapper.class, ClientAddressMapper.class, CarrierMapper.class, DriverMapper.class, TransferReasonMapper.class})
public interface RemissionGuideMapper {

  @Mapping(target = "documentSeriesId", source = "documentSeries.id")
  @Mapping(target = "client", source = "client")
  @Mapping(target = "clientAddress", source = "clientAddress")
  @Mapping(target = "selectedClientAddress", source = "selectedClientAddress")
  @Mapping(target = "carrier", source = "carrier")
  @Mapping(target = "items", source = "items")
  @Mapping(target = "drivers", source = "drivers")
  RemissionGuideResponse toResponse(RemissionGuide entity);

  List<RemissionGuideResponse> toResponseList(List<RemissionGuide> entities);

  @Mapping(target = "productId", source = "product.id")
  RemissionGuideItemResponse toItemResponse(RemissionGuideItem entity);

  @Mapping(target = "driver", source = "driver", qualifiedByName = "toDriverResponse")
  @Mapping(target = "driverVehicle", source = "driverVehicle")
  @Mapping(target = "vehiclePlate", source = "vehiclePlate")
  RemissionGuideDriverResponse toDriverResponse(RemissionGuideDriver entity);
}
