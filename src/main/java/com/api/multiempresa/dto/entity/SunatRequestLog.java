package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "sunat_request_log")
@Getter
@Setter
@NoArgsConstructor
public class SunatRequestLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  /** Tipo de documento SUNAT: 01, 03, 07, 08, 09 */
  @Column(name = "document_type", length = 5, nullable = false)
  private String documentType;

  @Column(name = "series", length = 10, nullable = false)
  private String series;

  @Column(name = "sequence", length = 10, nullable = false)
  private String sequence;

  /** Quién disparó el envío: "job-system" o "manual-resend" */
  @Column(name = "triggered_by", length = 50, nullable = false)
  private String triggeredBy;

  /** Fecha y hora exacta en que se realizó la petición al facturador */
  @Column(name = "sent_at", nullable = false)
  private LocalDateTime sentAt;

  /** JSON completo enviado al facturador (TEXT) */
  @Column(name = "request_json", columnDefinition = "TEXT", nullable = false)
  private String requestJson;

  /** JSON completo de la respuesta recibida del facturador (TEXT). Puede ser null si hubo error de red. */
  @Column(name = "response_json", columnDefinition = "TEXT")
  private String responseJson;

  /** HTTP status code de la respuesta (200, 500, etc.) */
  @Column(name = "http_status")
  private Integer httpStatus;

  /** true si SUNAT aceptó/procesó correctamente (2xx + success=true), false en cualquier error */
  @Column(name = "success", nullable = false)
  private boolean success;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
