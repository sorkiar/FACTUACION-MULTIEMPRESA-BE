package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ubigeo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ubigeo {

  @Id
  @Column(length = 8)
  private String ubigeo;

  @Column(length = 50)
  private String department;

  @Column(length = 50)
  private String province;

  @Column(name = "district", length = 50)
  private String district;

  @Column(nullable = false)
  private Integer status;
}
