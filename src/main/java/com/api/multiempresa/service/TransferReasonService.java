package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.TransferReasonFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.TransferReasonResponse;
import java.util.List;

public interface TransferReasonService {

  ApiResponse<List<TransferReasonResponse>> findAll(TransferReasonFilter filter);
}
