package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.request.CompanyRequest;
import com.api.multiempresa.dto.response.CompanyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

  CompanyResponse toResponse(Company company);

  Company toEntity(CompanyRequest request);

  void updateEntity(CompanyRequest request, @MappingTarget Company company);
}
