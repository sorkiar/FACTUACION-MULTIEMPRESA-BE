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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "configuration",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "config_group_key_company",
            columnNames = {"company_id", "config_group", "config_key"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(name = "config_group")
  private String configGroup;

  @Column(name = "config_key")
  private String configKey;

  @Column(name = "config_value", columnDefinition = "LONGTEXT")
  private String configValue;

  @Column(name = "config_datatype")
  private String configDatatype;

  @Column(length = 255)
  private String description;

  @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
  private Integer editable = 0;

  @Column(name = "sort_order", nullable = false, columnDefinition = "INT DEFAULT 0")
  private Integer sortOrder = 0;

  @Column(name = "col_span", nullable = false, columnDefinition = "INT DEFAULT 2")
  private Integer colSpan = 2;

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
