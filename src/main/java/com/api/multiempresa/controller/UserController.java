package com.api.multiempresa.controller;

import com.api.multiempresa.dto.filter.UserFilter;
import com.api.multiempresa.dto.request.ChangePasswordRequest;
import com.api.multiempresa.dto.request.UserRequest;
import com.api.multiempresa.dto.request.UserStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UserResponse;
import com.api.multiempresa.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
  private final UserService service;

  @GetMapping
  public ApiResponse<List<UserResponse>> list(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Long documentTypeId,
      @RequestParam(required = false) String documentNumber,
      @RequestParam(required = false) Integer status
  ) {
    UserFilter filter = new UserFilter();
    filter.setId(id);
    filter.setUsername(username);
    filter.setDocumentTypeId(documentTypeId);
    filter.setDocumentNumber(documentNumber);
    filter.setStatus(status);

    return service.findAll(filter);
  }

  @PutMapping("/{id}")
  public ApiResponse<UserResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody UserRequest request
  ) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody UserStatusRequest request
  ) {
    return service.updateStatus(id, request);
  }

  @PatchMapping("/me/password")
  public ApiResponse<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest request
  ) {
    return service.changePassword(request);
  }
}
