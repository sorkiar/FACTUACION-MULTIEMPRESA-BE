package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.request.CompanyRequest;
import com.api.multiempresa.dto.response.CompanyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

  @Mapping(target = "sunatCertificateLoaded",
      expression = "java(company.getSunatCertificatePublicKey() != null && !company.getSunatCertificatePublicKey().isBlank())")
  CompanyResponse toResponse(Company company);

  @Mapping(target = "sunatCertificatePublicKey", ignore = true)
  @Mapping(target = "sunatCertificatePrivateKey", ignore = true)
  @Mapping(target = "status", ignore = true)
  Company toEntity(CompanyRequest request);

  @Mapping(target = "sunatCertificatePublicKey", ignore = true)
  @Mapping(target = "sunatCertificatePrivateKey", ignore = true)
  @Mapping(target = "sunatSecondaryUserPassword", ignore = true)
  @Mapping(target = "sunatGuidePassword", ignore = true)
  @Mapping(target = "status", ignore = true)
  void updateEntity(CompanyRequest request, @MappingTarget Company company);
}
