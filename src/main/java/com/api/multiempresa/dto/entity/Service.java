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
    name = "service",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_service_sku_company", columnNames = {"company_id", "sku"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Service {
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
  @JoinColumn(name = "service_category_id", nullable = false)
  private ServiceCategory serviceCategory;

  @ManyToOne
  @JoinColumn(name = "charge_unit_id", nullable = false)
  private ChargeUnit chargeUnit;

  @ManyToOne
  @JoinColumn(name = "detraction_code_id")
  private DetractionCode detractionCode;

  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  // Precio en soles (PEN)
  @Column(name = "price_pen", precision = 12, scale = 2)
  private BigDecimal pricePen;

  // Precio en dólares (USD)
  @Column(name = "price_usd", precision = 12, scale = 2)
  private BigDecimal priceUsd;

  // Tiempo / entrega
  @Column(name = "estimated_time", length = 50)
  private String estimatedTime;

  @Column(name = "expected_delivery", length = 200)
  private String expectedDelivery;

  // Flags
  @Column(name = "requires_materials", nullable = false)
  private Boolean requiresMaterials;

  @Column(name = "requires_specification", nullable = false)
  private Boolean requiresSpecification;

  // Incluye / No incluye
  @Column(name = "includes_description", columnDefinition = "TEXT")
  private String includesDescription;

  @Column(name = "excludes_description", columnDefinition = "TEXT")
  private String excludesDescription;

  @Column(columnDefinition = "TEXT")
  private String conditions;

  // Descripción
  @Column(name = "short_description", columnDefinition = "TEXT", nullable = false)
  private String shortDescription;

  @Column(name = "detailed_description", columnDefinition = "TEXT", nullable = false)
  private String detailedDescription;

  // Archivos
  @Column(name = "image_url", length = 255)
  private String imageUrl;

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
