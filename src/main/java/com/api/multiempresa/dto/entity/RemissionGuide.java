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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(
    name = "remission_guide",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_rg_series_sequence",
            columnNames = {"document_series_id", "sequence"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemissionGuide {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne
  @JoinColumn(name = "document_series_id", nullable = false)
  private DocumentSeries documentSeries;

  @Column(length = 4, nullable = false)
  private String series;

  @Column(length = 8, nullable = false)
  private String sequence;

  @Column(name = "issue_date", nullable = false)
  private LocalDateTime issueDate;

  @Column(name = "transfer_date", nullable = false)
  private LocalDate transferDate;

  /** Catálogo 20 SUNAT: VENTA, COMPRA, TRASLADO_EMPRESA, OTROS, EXPORTACION, etc. */
  @Column(name = "transfer_reason", length = 30, nullable = false)
  private String transferReason;

  /** Descripción libre, obligatoria cuando transferReason = OTROS */
  @Column(name = "transfer_reason_description", length = 255)
  private String transferReasonDescription;

  /** Catálogo 18 SUNAT: TRANSPORTE_PUBLICO | TRANSPORTE_PRIVADO */
  @Column(name = "transport_mode", length = 30, nullable = false)
  private String transportMode;

  @Column(name = "gross_weight", precision = 14, scale = 3, nullable = false)
  private BigDecimal grossWeight;

  @Column(name = "weight_unit", length = 10, nullable = false)
  private String weightUnit = "KGM";

  @Column(name = "package_count", nullable = false)
  private Integer packageCount = 1;

  // Punto de partida
  @Column(name = "origin_address", length = 500, nullable = false)
  private String originAddress;

  @Column(name = "origin_ubigeo", length = 10, nullable = false)
  private String originUbigeo;

  @Column(name = "origin_local_code", length = 20)
  private String originLocalCode;

  // Punto de llegada
  @Column(name = "destination_address", length = 500)
  private String destinationAddress;

  @Column(name = "destination_ubigeo", length = 10)
  private String destinationUbigeo;

  @Column(name = "destination_local_code", length = 20)
  private String destinationLocalCode;

  @Column(name = "minor_vehicle_transfer", nullable = false)
  private Boolean minorVehicleTransfer = false;

  // Destinatario (cliente)
  @ManyToOne
  @JoinColumn(name = "client_id")
  private Client client;

  /** Dirección del cliente para el comprobante (texto plano del request). */
  @Column(name = "client_address", length = 500)
  private String clientAddress;

  /** Dirección registrada seleccionada del cliente (referencial, nullable). */
  @ManyToOne
  @JoinColumn(name = "client_address_id")
  private ClientAddress selectedClientAddress;

  // Transportista (solo para TRANSPORTE_PUBLICO)
  @ManyToOne
  @JoinColumn(name = "carrier_id")
  private Carrier carrier;

  @Column(columnDefinition = "TEXT")
  private String observations;

  // Estado y respuesta SUNAT
  @Column(length = 20, nullable = false)
  private String status = "PENDIENTE";

  @Column(name = "sunat_response_code")
  private Integer sunatResponseCode;

  @Column(name = "sunat_message", columnDefinition = "TEXT")
  private String sunatMessage;

  @Column(name = "sunat_ticket", length = 100)
  private String sunatTicket;

  @Column(name = "hash_code")
  private String hashCode;

  @Column(name = "xml_base64", columnDefinition = "TEXT")
  private String xmlBase64;

  @Column(name = "cdr_base64", columnDefinition = "TEXT")
  private String cdrBase64;

  @Column(name = "xml_url", length = 500)
  private String xmlUrl;

  @Column(name = "cdr_url", length = 500)
  private String cdrUrl;

  @Column(name = "pdf_url", length = 500)
  private String pdfUrl;

  // Ítems
  @BatchSize(size = 30)
  @OneToMany(mappedBy = "remissionGuide", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private Set<RemissionGuideItem> items = new HashSet<>();

  // Conductores / vehículos (TRANSPORTE_PRIVADO)
  @BatchSize(size = 30)
  @OneToMany(mappedBy = "remissionGuide", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private Set<RemissionGuideDriver> drivers = new HashSet<>();

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
