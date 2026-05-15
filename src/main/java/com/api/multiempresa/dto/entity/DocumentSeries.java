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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@BatchSize(size = 20)
@Entity
@Table(
    name = "document_series",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_doc_type_series_company",
            columnNames = {"company_id", "document_type_sunat_code", "series"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSeries {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne
  @JoinColumn(name = "document_type_sunat_code", nullable = false)
  private DocumentTypeSunat documentTypeSunat;

  @Column(name = "parent_document_type_code", length = 5)
  private String parentDocumentTypeCode;

  @Column(length = 4, nullable = false)
  private String series;

  @Column(name = "current_sequence", nullable = false)
  private Integer currentSequence;

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
