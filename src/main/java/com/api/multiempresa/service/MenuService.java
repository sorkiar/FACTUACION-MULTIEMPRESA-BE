package com.api.multiempresa.service;

import com.api.multiempresa.dto.request.MenuRequest;
import com.api.multiempresa.dto.request.MenuStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.MenuResponse;
import com.api.multiempresa.dto.response.SidebarItemResponse;
import java.util.List;

public interface MenuService {

  ApiResponse<List<MenuResponse>> findAll();

  ApiResponse<MenuResponse> findById(Long id);

  ApiResponse<MenuResponse> create(MenuRequest request);

  ApiResponse<MenuResponse> update(Long id, MenuRequest request);

  ApiResponse<Void> updateStatus(Long id, MenuStatusRequest request);

  ApiResponse<List<SidebarItemResponse>> getSidebar();

  ApiResponse<List<SidebarItemResponse>> getNavbar();

  ApiResponse<List<SidebarItemResponse>> getInternal();

  ApiResponse<List<String>> getAllowedPaths();
}
