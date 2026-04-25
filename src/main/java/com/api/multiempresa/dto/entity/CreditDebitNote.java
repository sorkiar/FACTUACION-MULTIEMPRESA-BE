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
import jakarta.persistence.Table;
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
@Table(name = "credit_debit_note")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditDebitNote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne
  @JoinColumn(name = "sale_id", nullable = false)
  private Sale sale;

  @ManyToOne
  @JoinColumn(name = "original_document_id", nullable = false)
  private Document originalDocument;

  @ManyToOne
  @JoinColumn(name = "document_type_sunat_code", nullable = false)
  private DocumentTypeSunat documentTypeSunat;

  @ManyToOne
  @JoinColumn(name = "document_series_id", nullable = false)
  private DocumentSeries documentSeries;

  @Column(length = 4, nullable = false)
  private String series;

  @Column(length = 8, nullable = false)
  private String sequence;

  @Column(name = "issue_date", nullable = false)
  private LocalDateTime issueDate;

  // C01-C13 para crédito, D01-D12 para débito
  @ManyToOne
  @JoinColumn(name = "credit_debit_note_type_code", nullable = false)
  private CreditDebitNoteType creditDebitNoteType;

  @Column(length = 500)
  private String reason;

  @Column(name = "subtotal_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal subtotalAmount = BigDecimal.ZERO;

  @Column(name = "tax_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "total_amount", precision = 14, scale = 2, nullable = false)
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Column(name = "tax_percentage", precision = 5, scale = 2, nullable = false)
  private BigDecimal taxPercentage = new BigDecimal("18");

  @Column(name = "currency_code", length = 4, nullable = false)
  private String currencyCode = "PEN";

  @Column(length = 20)
  private String status;

  @Column(name = "sunat_response_code")
  private Integer sunatResponseCode;

  @Column(name = "sunat_message", columnDefinition = "TEXT")
  private String sunatMessage;

  @Column(name = "hash_code")
  private String hashCode;

  @Column(name = "qr_code")
  private String qrCode;

  @Column(name = "xml_base64", columnDefinition = "TEXT")
  private String xmlBase64;

  @Column(name = "cdr_base64", columnDefinition = "TEXT")
  private String cdrBase64;

  @Column(name = "pdf_url")
  private String pdfUrl;

  @Column(name = "xml_url", length = 500)
  private String xmlUrl;

  @Column(name = "cdr_url", length = 500)
  private String cdrUrl;

  @BatchSize(size = 30)
  @OneToMany(mappedBy = "creditDebitNote", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private Set<CreditDebitNoteItem> items = new HashSet<>();

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
