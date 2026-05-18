package com.api.multiempresa.controller;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.PaymentMethodResponse;
import com.api.multiempresa.service.PaymentMethodService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

  private final PaymentMethodService service;

  @GetMapping
  public ApiResponse<List<PaymentMethodResponse>> list() {
    return service.findAllActive();
  }
}
