package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.ChargeUnitFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.ChargeUnitResponse;
import java.util.List;

public interface ChargeUnitService {

  ApiResponse<List<ChargeUnitResponse>> findAll(ChargeUnitFilter filter);
}
