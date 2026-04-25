package com.api.multiempresa.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RucRecordResponse {

  private String ruc;
  private String name;
  private String state;
  private String condition;
  private String address;
  private String fullAddress;
  private String department;
  private String province;
  private String district;
  private String ubigeoSunat;
  private String ubigeoDept;
  private String ubigeoProv;
  private String ubigeoDist;
  private Boolean isRetentionAgent;
  private Boolean isPerceptionAgent;
  private Boolean isPerceptionFuelAgent;
  private Boolean isGoodTaxpayer;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
