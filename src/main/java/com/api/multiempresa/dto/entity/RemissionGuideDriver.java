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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "remission_guide_driver")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemissionGuideDriver {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "remission_guide_id", nullable = false)
  @JsonBackReference
  private RemissionGuide remissionGuide;

  /** Conductor del maestro de conductores. */
  @ManyToOne
  @JoinColumn(name = "driver_id", nullable = false)
  private Driver driver;

  /** Placa específica del conductor usada en esta guía (nullable). */
  @ManyToOne
  @JoinColumn(name = "driver_vehicle_id")
  private DriverVehicle driverVehicle;

  /** Placa denormalizada (del maestro o ingresada manualmente). */
  @Column(name = "vehicle_plate", length = 20)
  private String vehiclePlate;

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
