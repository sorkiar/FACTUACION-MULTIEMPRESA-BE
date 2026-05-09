package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.TransferReason;
import com.api.multiempresa.dto.response.TransferReasonResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferReasonMapper {

  TransferReasonResponse toResponse(TransferReason entity);

  List<TransferReasonResponse> toResponseList(List<TransferReason> entities);
}
