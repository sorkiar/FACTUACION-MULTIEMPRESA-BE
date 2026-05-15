package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "exchange_rate",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_exchange_rate_date_type",
            columnNames = {"rate_date", "type"}))
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "rate_date", nullable = false)
  private LocalDate date;

  @Column(nullable = false, precision = 10, scale = 3)
  private BigDecimal value;

  /** C = Compra, V = Venta */
  @Column(length = 1, nullable = false)
  private String type;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt;
}
