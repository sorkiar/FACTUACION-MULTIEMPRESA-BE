package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.DocumentType;
import com.api.multiempresa.dto.entity.Profile;
import com.api.multiempresa.dto.entity.User;
import com.api.multiempresa.dto.filter.UserFilter;
import com.api.multiempresa.dto.mapper.UserMapper;
import com.api.multiempresa.dto.request.ChangePasswordRequest;
import com.api.multiempresa.dto.request.UserRequest;
import com.api.multiempresa.dto.request.UserStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UserResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.DocumentTypeRepository;
import com.api.multiempresa.repository.ProfileRepository;
import com.api.multiempresa.repository.UserRepository;
import com.api.multiempresa.repository.spec.UserSpecification;
import com.api.multiempresa.service.UserService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository repository;
  private final DocumentTypeRepository documentTypeRepository;
  private final ProfileRepository profileRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper mapper;
  private final CompanyRepository companyRepository;

  @Override
  public ApiResponse<List<UserResponse>> findAll(UserFilter filter) {
    return new ApiResponse<>(
        "Usuarios listados correctamente",
        mapper.toResponseList(
            repository.findAll(UserSpecification.byFilter(filter))
        )
    );
  }

  @Override
  public ApiResponse<UserResponse> create(UserRequest request) {
    Long companyId = TenantContext.getCompanyId();

    if (repository.existsByUsernameAndCompanyId(request.getUsername(), companyId)) {
      throw new BusinessValidationException("El usuario ya existe");
    }

    if (request.getPassword() == null || request.getPassword().isBlank()) {
      throw new BusinessValidationException("La contraseña es obligatoria");
    }

    DocumentType documentType = documentTypeRepository.findById(
        request.getDocumentTypeId()
    ).orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no encontrado"));

    Profile profile = profileRepository.findByIdAndCompany_IdAndIsSystemFalse(
        request.getProfileId(), companyId
    ).orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

    User user = new User();
    user.setDocumentType(documentType);
    user.setProfile(profile);
    user.setDocumentNumber(request.getDocumentNumber());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setPlainPassword(request.getPassword());
    user.setStatus(1);
    user.setCreatedBy(JwtUtils.extractUsernameFromContext());
    user.setCompany(companyRepository.getReferenceById(companyId));

    return new ApiResponse<>(
        "Usuario registrado correctamente",
        mapper.toResponse(repository.save(user))
    );
  }

  @Override
  public ApiResponse<UserResponse> update(Long id, UserRequest request) {

    User user = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    user.setDocumentNumber(request.getDocumentNumber());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setUpdatedBy(JwtUtils.extractUsernameFromContext());

    return new ApiResponse<>(
        "Usuario actualizado correctamente",
        mapper.toResponse(repository.save(user))
    );
  }

  @Override
  public ApiResponse<Void> updateStatus(Long id, UserStatusRequest request) {

    User user = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    user.setStatus(request.getStatus());
    user.setUpdatedBy(JwtUtils.extractUsernameFromContext());

    repository.save(user);

    return new ApiResponse<>("Estado actualizado correctamente", null);
  }

  @Override
  public ApiResponse<Void> changePassword(ChangePasswordRequest request) {
    String subject = JwtUtils.extractUsernameFromContext(); // "ruc:username"
    String[] parts = subject.split(":", 2);
    String ruc = parts[0];
    String username = parts[1];

    User user = repository.findByUsernameAndCompany_Ruc(username, ruc)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    user.setPlainPassword(request.getNewPassword());
    user.setUpdatedBy(subject);

    repository.save(user);

    return new ApiResponse<>("Contraseña actualizada correctamente", null);
  }
}
