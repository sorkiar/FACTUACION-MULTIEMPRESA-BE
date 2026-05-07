package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Menu;
import com.api.multiempresa.dto.entity.Profile;
import com.api.multiempresa.dto.filter.ProfileFilter;
import com.api.multiempresa.dto.mapper.MenuMapper;
import com.api.multiempresa.dto.mapper.ProfileMapper;
import com.api.multiempresa.dto.request.ProfileMenuRequest;
import com.api.multiempresa.dto.request.ProfileRequest;
import com.api.multiempresa.dto.request.ProfileStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.MenuResponse;
import com.api.multiempresa.dto.response.ProfileResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.MenuRepository;
import com.api.multiempresa.repository.ProfileRepository;
import com.api.multiempresa.repository.spec.ProfileSpecification;
import com.api.multiempresa.service.ProfileService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

  private final ProfileRepository repository;
  private final MenuRepository menuRepository;
  private final CompanyRepository companyRepository;
  private final ProfileMapper mapper;
  private final MenuMapper menuMapper;

  @Override
  public ApiResponse<List<ProfileResponse>> findAll(ProfileFilter filter) {
    filter.setCompanyId(TenantContext.getCompanyId());
    return new ApiResponse<>(
        "Perfiles listados correctamente",
        mapper.toResponseList(
            repository.findAll(ProfileSpecification.byFilter(filter))
        )
    );
  }

  @Override
  public ApiResponse<ProfileResponse> findById(Long id) {
    Profile profile = findOrThrow(id);
    return new ApiResponse<>("Perfil encontrado", mapper.toResponse(profile));
  }

  @Override
  public ApiResponse<ProfileResponse> create(ProfileRequest request) {
    Long companyId = TenantContext.getCompanyId();
    Profile profile = new Profile();
    profile.setCompany(companyRepository.getReferenceById(companyId));
    profile.setIsSystem(false);
    profile.setName(request.getName());
    profile.setStatus(request.getStatus());
    profile.setCreatedBy(JwtUtils.extractUsernameFromContext());
    return new ApiResponse<>(
        "Perfil registrado correctamente",
        mapper.toResponse(repository.save(profile))
    );
  }

  @Override
  public ApiResponse<ProfileResponse> update(Long id, ProfileRequest request) {
    Profile profile = findOrThrowEditable(id);
    profile.setName(request.getName());
    profile.setStatus(request.getStatus());
    profile.setUpdatedBy(JwtUtils.extractUsernameFromContext());
    return new ApiResponse<>(
        "Perfil actualizado correctamente",
        mapper.toResponse(repository.save(profile))
    );
  }

  @Override
  public ApiResponse<Void> updateStatus(Long id, ProfileStatusRequest request) {
    Profile profile = findOrThrowEditable(id);
    profile.setStatus(request.getStatus());
    profile.setUpdatedBy(JwtUtils.extractUsernameFromContext());
    repository.save(profile);
    return new ApiResponse<>("Estado actualizado correctamente", null);
  }

  @Override
  @Transactional
  public ApiResponse<List<MenuResponse>> getMenus(Long id) {
    Profile profile = findOrThrow(id);
    List<Menu> menus = new ArrayList<>(profile.getMenus());
    menus.sort((a, b) -> {
      int cmp = a.getSortOrder().compareTo(b.getSortOrder());
      return cmp != 0 ? cmp : a.getId().compareTo(b.getId());
    });
    return new ApiResponse<>("Permisos listados correctamente", menuMapper.toResponseList(menus));
  }

  @Override
  @Transactional
  public ApiResponse<Void> updateMenus(Long id, ProfileMenuRequest request) {
    Profile profile = findOrThrowEditable(id);
    List<Menu> menus = menuRepository.findAllById(request.getMenuIds());
    profile.setMenus(new HashSet<>(menus));
    profile.setUpdatedBy(JwtUtils.extractUsernameFromContext());
    repository.save(profile);
    return new ApiResponse<>("Permisos actualizados correctamente", null);
  }

  // ── Private helpers ───────────────────────────────────────

  /** Finds profile by id, scoped to current tenant company. */
  private Profile findOrThrow(Long id) {
    Long companyId = TenantContext.getCompanyId();
    return repository.findByIdAndCompany_IdAndIsSystemFalse(id, companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));
  }

  /** Same as findOrThrow but also blocks system profiles from being mutated. */
  private Profile findOrThrowEditable(Long id) {
    if (repository.existsByIdAndIsSystemTrue(id)) {
      throw new BusinessValidationException("El perfil SISTEMAS es inmutable y no puede modificarse");
    }
    return findOrThrow(id);
  }
}
