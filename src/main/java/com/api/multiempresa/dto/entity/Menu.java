package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
public class Menu {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 100, nullable = false)
  private String name;

  @Column(length = 200)
  private String path;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Menu parent;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  @Column(nullable = false)
  private Integer status;

  @Column(name = "menu_type", length = 20, nullable = false)
  private String menuType;
}
