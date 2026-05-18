package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.PaymentMethod;
import com.api.multiempresa.dto.response.PaymentMethodResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {
  PaymentMethodResponse toResponse(PaymentMethod entity);
  List<PaymentMethodResponse> toResponseList(List<PaymentMethod> entities);
}
