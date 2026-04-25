package com.api.multiempresa.service;

import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.DashboardResponse;
import com.api.multiempresa.dto.response.MonthlyRevenueResponse;

public interface DashboardService {

  ApiResponse<DashboardResponse> getDashboard();

  ApiResponse<MonthlyRevenueResponse> getMonthlyRevenue(int year, int month);
}
