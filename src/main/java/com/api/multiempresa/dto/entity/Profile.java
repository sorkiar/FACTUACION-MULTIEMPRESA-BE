package com.api.multiempresa.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "company_id")
  private Company company;

  @Column(name = "is_system", nullable = false)
  private Boolean isSystem = false;

  @Column(length = 20)
  private String code;

  @Column(length = 100, nullable = false)
  private String name;

  private Integer status;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "profile_menu",
      joinColumns = @JoinColumn(name = "profile_id"),
      inverseJoinColumns = @JoinColumn(name = "menu_id")
  )
  private Set<Menu> menus = new HashSet<>();

  // Auditoría
  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "created_by", updatable = false)
  private String createdBy;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private String deletedBy;
}
