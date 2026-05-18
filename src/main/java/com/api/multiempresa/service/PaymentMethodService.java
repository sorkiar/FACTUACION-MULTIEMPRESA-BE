package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.PaymentMethodResponse;
import java.util.List;

public interface PaymentMethodService {
  ApiResponse<List<PaymentMethodResponse>> findAllActive();
}
