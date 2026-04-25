package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "product",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_sku_company", columnNames = {"company_id", "sku"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Identificación
  @Column(length = 50, nullable = false)
  private String sku;

  @Column(length = 200, nullable = false)
  private String name;

  // Relaciones
  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @ManyToOne
  @JoinColumn(name = "unit_measure_id", nullable = false)
  private UnitMeasure unitMeasure;

  @ManyToOne
  @JoinColumn(name = "detraction_code_id")
  private DetractionCode detractionCode;

  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  // Comercial — precios en soles (PEN)
  @Column(name = "sale_price_pen", precision = 12, scale = 2)
  private BigDecimal salePricePen;

  @Column(name = "estimated_cost_pen", precision = 12, scale = 2)
  private BigDecimal estimatedCostPen;

  // Comercial — precios en dólares (USD)
  @Column(name = "sale_price_usd", precision = 12, scale = 2)
  private BigDecimal salePriceUsd;

  @Column(name = "estimated_cost_usd", precision = 12, scale = 2)
  private BigDecimal estimatedCostUsd;

  // Marca / modelo
  @Column(length = 100)
  private String brand;

  @Column(length = 100)
  private String model;

  // Descripción
  @Column(name = "short_description", length = 500, nullable = false)
  private String shortDescription;

  @Column(name = "technical_spec", columnDefinition = "TEXT", nullable = false)
  private String technicalSpec;

  // Archivos
  @Column(name = "main_image_url", length = 255, nullable = false)
  private String mainImageUrl;

  @Column(name = "technical_sheet_url", length = 255)
  private String technicalSheetUrl;

  // Estado
  @Column(nullable = false)
  private Integer status;

  // Auditoría
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

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 50)
  private String deletedBy;
}
