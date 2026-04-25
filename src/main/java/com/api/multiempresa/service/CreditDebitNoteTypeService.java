package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.CreditDebitNoteTypeFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CreditDebitNoteTypeResponse;
import java.util.List;

public interface CreditDebitNoteTypeService {
  ApiResponse<List<CreditDebitNoteTypeResponse>> findAll(CreditDebitNoteTypeFilter filter);
}
