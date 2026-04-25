package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.User;
import com.api.multiempresa.dto.response.UserResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(source = "documentType.id", target = "documentTypeId")
  @Mapping(source = "documentType.name", target = "documentTypeName")
  @Mapping(source = "profile.id", target = "profileId")
  @Mapping(source = "profile.name", target = "profileName")
  UserResponse toResponse(User entity);
  List<UserResponse> toResponseList(List<User> entities);
}
