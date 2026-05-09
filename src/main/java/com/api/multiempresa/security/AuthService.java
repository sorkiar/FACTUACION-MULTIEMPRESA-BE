package com.api.multiempresa.security;

import com.api.multiempresa.dto.request.AuthRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.UserAuthResponse;
import com.api.multiempresa.exception.LoginException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.UserRepository;
import com.api.multiempresa.util.JwtUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;

  public ApiResponse<UserAuthResponse> login(AuthRequest request) {
    com.api.multiempresa.dto.entity.User user =
        userRepository.findByUsernameAndCompany_Ruc(request.getUsername(), request.getRuc())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado o credenciales inválidas"));

    if (user.getStatus() == 0) {
      throw new LoginException("Usuario inactivo, contacte con el administrador", 403);
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new LoginException("Contraseña incorrecta", 401);
    }

    String profileCode = user.getProfile().getCode() != null
        ? user.getProfile().getCode()
        : user.getProfile().getName().toUpperCase().replace(" ", "_");

    // JWT subject = "ruc:username" to uniquely identify user across tenants
    String subject = request.getRuc() + ":" + request.getUsername();

    UserDetails userDetails = new User(
        subject,
        user.getPassword(),
        List.of(new SimpleGrantedAuthority("ROLE_" + profileCode))
    );

    Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;
    String companyName = user.getCompany() != null ? user.getCompany().getBusinessName() : null;

    UserAuthResponse response = new UserAuthResponse();
    response.setId(user.getId());
    response.setDocumentType(user.getDocumentType().getName());
    response.setDocumentNumber(user.getDocumentNumber());
    response.setProfileId(user.getProfile().getId());
    response.setProfileCode(user.getProfile().getCode());
    response.setProfile(user.getProfile().getName());
    response.setFirstName(user.getFirstName());
    response.setLastName(user.getLastName());
    response.setUsername(user.getUsername());
    response.setToken(jwtUtils.generateToken(userDetails, companyId));
    response.setStatus(user.getStatus());
    response.setCompanyId(companyId);
    response.setCompanyName(companyName);

    return new ApiResponse<>(
        "Inicio de sesión exitoso",
        response
    );
  }
}
