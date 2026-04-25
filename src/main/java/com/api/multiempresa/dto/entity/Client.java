package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "person_type_id", nullable = false)
  private PersonType personType;

  @ManyToOne
  @JoinColumn(name = "document_type_id", nullable = false)
  private DocumentType documentType;

  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  // Identificación
  @Column(name = "document_number", length = 20, nullable = false)
  private String documentNumber;

  // Persona Natural
  private String firstName;
  private String lastName;
  private LocalDate birthDate;

  // Persona Jurídica
  private String businessName;
  private String contactPersonName;

  // Contacto
  @Column(name = "country_code_1", length = 10)
  private String countryCode1;
  @Column(name = "phone_1", length = 25)
  private String phone1;
  @Column(name = "country_code_2", length = 10)
  private String countryCode2;
  @Column(name = "phone_2", length = 25)
  private String phone2;
  @Column(name = "email_1", length = 150)
  private String email1;
  @Column(name = "email_2", length = 150)
  private String email2;

  // Direcciones (excluye soft-deleted automáticamente)
  @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
  @SQLRestriction("deleted_at IS NULL")
  @BatchSize(size = 30)
  private List<ClientAddress> addresses = new ArrayList<>();

  // Retención
  @Column(name = "retention_agent", nullable = false)
  private Boolean retentionAgent;

  // Estado
  private Integer status;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "created_by", length = 50, updatable = false)
  private String createdBy;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Column(name = "updated_by", length = 50)
  private String updatedBy;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 50)
  private String deletedBy;
}
