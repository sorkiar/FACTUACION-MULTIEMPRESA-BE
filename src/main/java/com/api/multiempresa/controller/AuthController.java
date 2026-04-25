package com.api.multiempresa.controller;

import com.api.multiempresa.dto.request.AuthRequest;
import com.api.multiempresa.dto.request.UserRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UserAuthResponse;
import com.api.multiempresa.dto.response.UserResponse;
import com.api.multiempresa.security.AuthService;
import com.api.multiempresa.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
  private final AuthService service;
  private final UserService userService;

  @PostMapping("/login")
  public ApiResponse<UserAuthResponse> login(
      @Valid @RequestBody AuthRequest request) {
    return service.login(request);
  }

  @PostMapping("/register")
  public ApiResponse<UserResponse> create(
      @Valid @RequestBody UserRequest request
  ) {
    return userService.create(request);
  }
}
