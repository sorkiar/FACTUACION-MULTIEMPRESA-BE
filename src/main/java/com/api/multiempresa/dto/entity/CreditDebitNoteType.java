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

/**
 * Catálogo 09/10 de SUNAT.
 * Crédito: C01-C13  |  Débito: D01-D12
 */
@Entity
@Table(name = "credit_debit_note_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditDebitNoteType {

  /** Código SUNAT: C01, C06, D01, D02, etc. */
  @Id
  @Column(length = 4)
  private String code;

  @Column(length = 150, nullable = false)
  private String name;

  /** "CREDITO" o "DEBITO" */
  @Column(name = "note_category", length = 10, nullable = false)
  private String noteCategory;

  @Column(nullable = false)
  private Integer status;

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
