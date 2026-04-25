package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ruc_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RucRecord {

  @Id
  @Column(name = "ruc", length = 11)
  private String ruc;

  @Column(length = 300)
  private String name;

  @Column(length = 50)
  private String state;

  @Column(name = "condition_record", length = 50)
  private String condition;

  @Column(length = 500)
  private String address;

  @Column(name = "full_address", length = 500)
  private String fullAddress;

  @Column(length = 100)
  private String department;

  @Column(length = 100)
  private String province;

  @Column(length = 100)
  private String district;

  @Column(name = "ubigeo_sunat", length = 10)
  private String ubigeoSunat;

  @Column(name = "ubigeo_dept", length = 10)
  private String ubigeoDept;

  @Column(name = "ubigeo_prov", length = 10)
  private String ubigeoProv;

  @Column(name = "ubigeo_dist", length = 10)
  private String ubigeoDist;

  @Column(name = "is_retention_agent", nullable = false)
  private Boolean isRetentionAgent = false;

  @Column(name = "is_perception_agent", nullable = false)
  private Boolean isPerceptionAgent = false;

  @Column(name = "is_perception_fuel_agent", nullable = false)
  private Boolean isPerceptionFuelAgent = false;

  @Column(name = "is_good_taxpayer", nullable = false)
  private Boolean isGoodTaxpayer = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "created_by", length = 50, nullable = false, updatable = false)
  private String createdBy;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "updated_by", length = 50)
  private String updatedBy;
}
