package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.ProfileFilter;
import com.api.multiempresa.dto.request.ProfileMenuRequest;
import com.api.multiempresa.dto.request.ProfileRequest;
import com.api.multiempresa.dto.request.ProfileStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.MenuResponse;
import com.api.multiempresa.dto.response.ProfileResponse;
import java.util.List;

public interface ProfileService {

  ApiResponse<List<ProfileResponse>> findAll(ProfileFilter filter);

  ApiResponse<ProfileResponse> findById(Long id);

  ApiResponse<ProfileResponse> create(ProfileRequest request);

  ApiResponse<ProfileResponse> update(Long id, ProfileRequest request);

  ApiResponse<Void> updateStatus(Long id, ProfileStatusRequest request);

  ApiResponse<List<MenuResponse>> getMenus(Long id);

  ApiResponse<Void> updateMenus(Long id, ProfileMenuRequest request);
}
