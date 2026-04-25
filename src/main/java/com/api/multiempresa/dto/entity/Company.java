package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "ruc", length = 11, nullable = false, unique = true)
  private String ruc;

  @Column(name = "business_name", length = 200, nullable = false)
  private String businessName;

  @Column(name = "trade_name", length = 200)
  private String tradeName;

  @Column(name = "address", length = 500)
  private String address;

  @Column(name = "ubigeo", length = 10)
  private String ubigeo;

  @Column(name = "phone", length = 30)
  private String phone;

  @Column(name = "email", length = 150)
  private String email;

  @Column(name = "website", length = 200)
  private String website;

  @Column(name = "logo_url", length = 500)
  private String logoUrl;

  @Column(name = "status", nullable = false)
  private Integer status = 1;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "created_by", length = 50, updatable = false)
  private String createdBy;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Column(name = "updated_by", length = 50)
  private String updatedBy;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 50)
  private String deletedBy;
}
