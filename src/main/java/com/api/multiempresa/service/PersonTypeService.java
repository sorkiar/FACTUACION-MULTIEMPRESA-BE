package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.PersonTypeFilter;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.PersonTypeResponse;
import java.util.List;

public interface PersonTypeService {
  ApiResponse<List<PersonTypeResponse>> findAll(PersonTypeFilter filter);
}
