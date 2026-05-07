package com.api.multiempresa.security;

import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Called by JwtAuthFilter on every authenticated request.
   * The {@code subject} parameter is "ruc:username" as stored in the JWT.
   */
  @Override
  public UserDetails loadUserByUsername(String subject) throws UsernameNotFoundException {
    String[] parts = subject.split(":", 2);
    if (parts.length != 2) {
      throw new UsernameNotFoundException("Formato de sujeto JWT inválido: " + subject);
    }
    String ruc = parts[0];
    String username = parts[1];

    com.api.multiempresa.dto.entity.User user =
        userRepository.findByUsernameAndCompany_Ruc(username, ruc)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado o credenciales inválidas"));

    String profileCode = user.getProfile().getCode() != null
        ? user.getProfile().getCode()
        : user.getProfile().getName().toUpperCase().replace(" ", "_");

    return new User(
        subject,
        user.getPassword(),
        List.of(new SimpleGrantedAuthority("ROLE_" + profileCode))
    );
  }
}
