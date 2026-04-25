package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.UnitMeasureFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UnitMeasureResponse;
import java.util.List;

public interface UnitMeasureService {
  ApiResponse<List<UnitMeasureResponse>> findAll(UnitMeasureFilter filter);
}
