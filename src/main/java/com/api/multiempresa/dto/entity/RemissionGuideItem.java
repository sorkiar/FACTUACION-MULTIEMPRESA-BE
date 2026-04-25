package com.api.multiempresa.dto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "remission_guide_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemissionGuideItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "remission_guide_id", nullable = false)
  @JsonBackReference
  private RemissionGuide remissionGuide;

  /** Referencia opcional al catálogo de productos */
  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  @Column(length = 500, nullable = false)
  private String description;

  @Column(precision = 14, scale = 3, nullable = false)
  private BigDecimal quantity;

  /** Código SUNAT de la unidad de medida (ej: NIU, KGM, ZZ) */
  @Column(name = "unit_measure_sunat", length = 10, nullable = false)
  private String unitMeasureSunat = "NIU";

  /** Precio unitario con IGV */
  @Column(name = "unit_price", precision = 14, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "subtotal_amount", precision = 14, scale = 2)
  private BigDecimal subtotalAmount;

  @Column(name = "tax_amount", precision = 14, scale = 2)
  private BigDecimal taxAmount;

  @Column(name = "total_amount", precision = 14, scale = 2)
  private BigDecimal totalAmount;

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
