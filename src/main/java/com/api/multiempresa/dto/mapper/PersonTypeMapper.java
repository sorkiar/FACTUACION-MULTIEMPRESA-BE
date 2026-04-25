package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.PersonType;
import com.api.multiempresa.dto.response.PersonTypeResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonTypeMapper {
  PersonTypeResponse toResponse(PersonType entity);
  List<PersonTypeResponse> toResponseList(List<PersonType> entities);
}
