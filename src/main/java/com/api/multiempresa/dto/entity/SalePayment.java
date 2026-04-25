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
@Table(name = "sale_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalePayment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 🔵 Relación con venta
  @ManyToOne
  @JoinColumn(name = "sale_id", nullable = false)
  @JsonBackReference
  private Sale sale;

  // 🔵 Método de pago
  @ManyToOne
  @JoinColumn(name = "payment_method_id", nullable = false)
  private PaymentMethod paymentMethod;

  @Column(name = "amount_paid", precision = 14, scale = 2, nullable = false)
  private BigDecimal amountPaid;

  @Column(name = "change_amount", precision = 14, scale = 2)
  private BigDecimal changeAmount;

  @Column(name = "payment_date", nullable = false)
  private LocalDateTime paymentDate;

  @Column(name = "payment_reference", length = 150)
  private String paymentReference;

  @Column(name = "proof_file_url")
  private String proofFileUrl;

  @Column(columnDefinition = "TEXT")
  private String observations;

  // 🔵 Auditoría
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
