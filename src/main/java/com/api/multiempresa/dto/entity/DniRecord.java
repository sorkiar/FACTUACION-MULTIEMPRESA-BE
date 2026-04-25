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
@Table(name = "dni_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DniRecord {

  @Id
  @Column(name = "doc_number", length = 8)
  private String docNumber;

  @Column(name = "full_name", length = 200)
  private String fullName;

  @Column(name = "first_name", length = 100)
  private String firstName;

  @Column(name = "last_name_paternal", length = 100)
  private String lastNamePaternal;

  @Column(name = "last_name_maternal", length = 100)
  private String lastNameMaternal;

  @Column(name = "verification_code", length = 10)
  private String verificationCode;

  @Column(length = 500)
  private String address;

  @Column(name = "full_address", length = 500)
  private String fullAddress;

  @Column(name = "ubigeo_reniec", length = 10)
  private String ubigeoReniec;

  @Column(name = "ubigeo_sunat", length = 10)
  private String ubigeoSunat;

  @Column(name = "ubigeo_dept", length = 10)
  private String ubigeoDept;

  @Column(name = "ubigeo_prov", length = 10)
  private String ubigeoProv;

  @Column(name = "ubigeo_dist", length = 10)
  private String ubigeoDist;

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
