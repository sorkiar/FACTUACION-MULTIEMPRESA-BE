package com.api.multiempresa.service;

import com.api.multiempresa.dto.filter.UserFilter;
import com.api.multiempresa.dto.request.ChangePasswordRequest;
import com.api.multiempresa.dto.request.UserRequest;
import com.api.multiempresa.dto.request.UserStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UserResponse;
import java.util.List;

public interface UserService {
  ApiResponse<List<UserResponse>> findAll(UserFilter filter);

  ApiResponse<UserResponse> create(UserRequest request);

  ApiResponse<UserResponse> update(Long id, UserRequest request);

  ApiResponse<Void> updateStatus(Long id, UserStatusRequest request);

  ApiResponse<Void> changePassword(ChangePasswordRequest request);
}
