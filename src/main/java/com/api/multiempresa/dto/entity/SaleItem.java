package com.api.multiempresa.dto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.api.multiempresa.dto.entity.DetractionCode;
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
@Table(name = "sale_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "sale_id", nullable = false)
  @JsonBackReference
  private Sale sale;

  @Column(name = "item_type", length = 20, nullable = false)
  private String itemType;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne
  @JoinColumn(name = "service_id")
  private Service service;

  @ManyToOne
  @JoinColumn(name = "detraction_code_id")
  private DetractionCode detractionCode;

  @Column(length = 500, nullable = false)
  private String description;

  @Column(precision = 14, scale = 2, nullable = false)
  private BigDecimal quantity;

  @Column(name = "unit_price", precision = 14, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  @Column(name = "discount_percentage", precision = 5, scale = 2)
  private BigDecimal discountPercentage;

  @Column(name = "subtotal_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal subtotalAmount;

  @Column(name = "tax_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal taxAmount;

  @Column(name = "total_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal totalAmount;

  @ManyToOne
  @JoinColumn(name = "unit_measure_id")
  private UnitMeasure unitMeasure;

  // Audit
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
