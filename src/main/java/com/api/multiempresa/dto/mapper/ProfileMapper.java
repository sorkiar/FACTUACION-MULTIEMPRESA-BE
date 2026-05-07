package com.api.multiempresa.dto.mapper;

import com.api.multiempresa.dto.entity.Profile;
import com.api.multiempresa.dto.response.ProfileResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

  ProfileResponse toResponse(Profile entity);

  List<ProfileResponse> toResponseList(List<Profile> entities);
}
