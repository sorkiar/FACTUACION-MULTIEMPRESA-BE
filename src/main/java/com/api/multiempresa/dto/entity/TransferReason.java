package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transfer_reason")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferReason {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** SUNAT Catalog 20 code (e.g. "01", "13") */
  @Column(length = 5, nullable = false, unique = true)
  private String code;

  @Column(length = 150, nullable = false)
  private String name;

  /** 1 = active, 0 = inactive */
  @Column(nullable = false)
  private Integer status;
}
