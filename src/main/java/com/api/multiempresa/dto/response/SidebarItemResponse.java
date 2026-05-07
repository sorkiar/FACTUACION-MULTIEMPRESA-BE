package com.api.multiempresa.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class SidebarItemResponse {
  private String name;
  private String path;
  private List<SidebarSubItemResponse> subItems;
}
