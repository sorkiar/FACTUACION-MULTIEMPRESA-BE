package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.CreditDebitNoteFilter;
import com.api.multiempresa.dto.request.CreditDebitNoteRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CreditDebitNoteResponse;
import java.util.List;

public interface CreditDebitNoteService {

  ApiResponse<List<CreditDebitNoteResponse>> findAll(CreditDebitNoteFilter filter);

  ApiResponse<CreditDebitNoteResponse> create(CreditDebitNoteRequest request);

  ApiResponse<CreditDebitNoteResponse> findById(Long id);
}
