package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Menu;
import com.api.multiempresa.dto.response.MenuResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuMapper {

  @Mapping(source = "parent.id", target = "parentId")
  @Mapping(source = "status", target = "status")
  MenuResponse toResponse(Menu entity);

  List<MenuResponse> toResponseList(List<Menu> entities);
}
