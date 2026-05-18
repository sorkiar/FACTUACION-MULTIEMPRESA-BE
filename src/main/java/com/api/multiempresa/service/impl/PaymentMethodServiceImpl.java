package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.mapper.PaymentMethodMapper;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.PaymentMethodResponse;
import com.api.multiempresa.repository.PaymentMethodRepository;
import com.api.multiempresa.service.PaymentMethodService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

  private final PaymentMethodRepository repository;
  private final PaymentMethodMapper mapper;

  @Override
  public ApiResponse<List<PaymentMethodResponse>> findAllActive() {
    return new ApiResponse<>(
        "Métodos de pago listados correctamente",
        mapper.toResponseList(repository.findByStatusOrderByIdAsc(1))
    );
  }
}
