package com.api.multiempresa.dto.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne
  @JoinColumn(name = "client_id")
  private Client client;

  /** ID de dirección registrada del cliente (referencial, nullable). */
  @Column(name = "client_address_id")
  private Long clientAddressId;

  /** Dirección del cliente para el comprobante (texto plano). */
  @Column(name = "client_address", length = 500)
  private String clientAddress;

  @Column(name = "sale_status", length = 20, nullable = false)
  private String saleStatus;

  @Column(name = "subtotal_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal subtotalAmount = BigDecimal.ZERO;

  @Column(name = "discount_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "tax_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "total_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Column(name = "currency_code", length = 4, nullable = false)
  private String currencyCode;

  @Column(name = "tax_percentage", precision = 5, scale = 2, nullable = false)
  private BigDecimal taxPercentage;

  @Column(name = "sale_date", nullable = false)
  private LocalDateTime saleDate;

  @Column(name = "payment_type", length = 10, nullable = false)
  private String paymentType = "CONTADO";

  @Column(name = "purchase_order", length = 50)
  private String purchaseOrder;

  @Column(columnDefinition = "TEXT")
  private String observations;

  // Retención
  @Column(name = "has_retention", nullable = false)
  private Boolean hasRetention = false;

  @Column(name = "retention_amount", precision = 14, scale = 2)
  private BigDecimal retentionAmount;

  @Column(name = "retention_rate", precision = 5, scale = 2)
  private BigDecimal retentionRate;

  // Detracción
  @Column(name = "has_detraction", nullable = false)
  private Boolean hasDetraction = false;

  @Column(name = "detraction_code", length = 3)
  private String detractionCode;

  @Column(name = "detraction_rate", precision = 5, scale = 2)
  private BigDecimal detractionRate;

  /** Always stored in PEN, even for USD invoices. */
  @Column(name = "detraction_amount", precision = 14, scale = 2)
  private BigDecimal detractionAmount;

  /** Monto neto a pagar en moneda de la venta: totalAmount menos retención local (si aplica).
   *  retentionAmount se almacena en PEN; se usa retentionRate para calcular el equivalente local. */
  @Transient
  public BigDecimal getNetAmount() {
    if (Boolean.TRUE.equals(hasRetention) && retentionRate != null) {
      BigDecimal retLocal = totalAmount
          .multiply(retentionRate)
          .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
      return totalAmount.subtract(retLocal);
    }
    return totalAmount;
  }

  @BatchSize(size = 30)
  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private Set<SaleItem> items = new HashSet<>();

  @BatchSize(size = 30)
  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private Set<Document> documents = new HashSet<>();

  @BatchSize(size = 30)
  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private Set<SalePayment> payments = new HashSet<>();

  @BatchSize(size = 30)
  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("installmentNumber ASC")
  @JsonManagedReference
  private Set<SaleInstallment> installments = new HashSet<>();

  @BatchSize(size = 20)
  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private Set<SaleRelatedGuide> relatedGuides = new HashSet<>();

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
