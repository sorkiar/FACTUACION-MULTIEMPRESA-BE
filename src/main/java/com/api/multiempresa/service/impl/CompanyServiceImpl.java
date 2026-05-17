package com.api.multiempresa.service.impl;

import com.api.multiempresa.dto.entity.Company;
import com.api.multiempresa.dto.entity.DocumentType;
import com.api.multiempresa.dto.entity.Profile;
import com.api.multiempresa.dto.entity.User;
import com.api.multiempresa.dto.mapper.CompanyMapper;
import com.api.multiempresa.dto.request.CompanyRequest;
import com.api.multiempresa.dto.request.CompanyStatusRequest;
import com.api.multiempresa.dto.response.ApiResponse;
import com.api.multiempresa.dto.response.CompanyResponse;
import com.api.multiempresa.exception.BusinessValidationException;
import com.api.multiempresa.exception.ResourceNotFoundException;
import com.api.multiempresa.repository.CompanyRepository;
import com.api.multiempresa.repository.DocumentTypeRepository;
import com.api.multiempresa.repository.MenuRepository;
import com.api.multiempresa.repository.ProfileRepository;
import com.api.multiempresa.repository.UserRepository;
import com.api.multiempresa.service.CompanyService;
import com.api.multiempresa.service.GoogleDriveService;
import com.api.multiempresa.util.JwtUtils;
import com.api.multiempresa.util.TenantContext;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.text.Normalizer;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {

  private static final String SYSTEMS_DEFAULT_PASSWORD = "SuperAdmin2026!";

  private final CompanyRepository companyRepository;
  private final CompanyMapper companyMapper;
  private final ProfileRepository profileRepository;
  private final MenuRepository menuRepository;
  private final UserRepository userRepository;
  private final DocumentTypeRepository documentTypeRepository;
  private final PasswordEncoder passwordEncoder;
  private final GoogleDriveService googleDriveService;

  @org.springframework.beans.factory.annotation.Value("${drive.folder-id.logos}")
  private String logosFolderId;

  @Override
  @Transactional
  public ApiResponse<CompanyResponse> create(CompanyRequest request, MultipartFile logoFile,
      MultipartFile pfxFile, String pfxPassword) {
    if (companyRepository.existsByRucAndDeletedAtIsNull(request.getRuc())) {
      throw new BusinessValidationException("Ya existe una empresa registrada con ese RUC");
    }

    normalizeUbigeoFields(request);
    Company company = companyMapper.toEntity(request);
    applyLogoIfProvided(company, logoFile, request.getRuc());
    company.setCreatedBy(JwtUtils.extractUsernameFromContext());
    applyPfxIfProvided(company, pfxFile, pfxPassword);
    companyRepository.save(company);
    provisionSystemsProfile(company);
    return new ApiResponse<>("Empresa creada", companyMapper.toResponse(company));
  }

  @Override
  @Transactional
  public ApiResponse<CompanyResponse> update(Long id, CompanyRequest request, MultipartFile logoFile,
      MultipartFile pfxFile, String pfxPassword) {
    Company company = findCompanyOrThrow(id);

    if (companyRepository.existsByRucAndDeletedAtIsNullAndIdNot(request.getRuc(), id)) {
      throw new BusinessValidationException("Ya existe otra empresa registrada con ese RUC");
    }

    normalizeUbigeoFields(request);
    companyMapper.updateEntity(request, company);
    applyPasswordIfProvided(company::setSunatSecondaryUserPassword, request.getSunatSecondaryUserPassword());
    applyPasswordIfProvided(company::setSunatGuidePassword, request.getSunatGuidePassword());
    applyLogoIfProvided(company, logoFile, request.getRuc());
    company.setUpdatedBy(JwtUtils.extractUsernameFromContext());
    applyPfxIfProvided(company, pfxFile, pfxPassword);
    return new ApiResponse<>("Empresa actualizada", companyMapper.toResponse(company));
  }

  private void applyPasswordIfProvided(java.util.function.Consumer<String> setter, String value) {
    if (value != null && !value.isBlank()) {
      setter.accept(value);
    }
  }

  private void applyLogoIfProvided(Company company, MultipartFile logoFile, String ruc) {
    if (logoFile == null || logoFile.isEmpty()) return;
    try {
      String originalName = logoFile.getOriginalFilename();
      String ext = (originalName != null && originalName.contains("."))
          ? originalName.substring(originalName.lastIndexOf('.'))
          : ".png";
      java.io.File tempFile = java.io.File.createTempFile("logo-" + ruc + "-", ext);
      logoFile.transferTo(tempFile);
      java.io.File namedFile = new java.io.File(
          tempFile.getParent(), "logo-" + ruc + ext);
      tempFile.renameTo(namedFile);
      String url = googleDriveService.uploadLogo(namedFile, logosFolderId);
      namedFile.delete();
      company.setLogoUrl(url);
    } catch (Exception e) {
      log.warn("No se pudo subir el logo de la empresa {}: {}", ruc, e.getMessage());
    }
  }

  private void normalizeUbigeoFields(CompanyRequest request) {
    request.setUbigDepartment(toUpperCaseNoAccents(request.getUbigDepartment()));
    request.setUbigProvince(toUpperCaseNoAccents(request.getUbigProvince()));
    request.setUbigDistrict(toUpperCaseNoAccents(request.getUbigDistrict()));
  }

  private String toUpperCaseNoAccents(String value) {
    if (value == null || value.isBlank()) return value;
    String normalized = Normalizer.normalize(value.toUpperCase(), Normalizer.Form.NFD);
    return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<CompanyResponse> findById(Long id) {
    return new ApiResponse<>("OK", companyMapper.toResponse(findCompanyOrThrow(id)));
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<List<CompanyResponse>> findAll() {
    List<CompanyResponse> list = companyRepository.findByDeletedAtIsNull()
        .stream().map(companyMapper::toResponse).toList();
    return new ApiResponse<>("OK", list);
  }

  @Override
  @Transactional
  public ApiResponse<Void> updateStatus(Long id, CompanyStatusRequest request) {
    Company company = findCompanyOrThrow(id);
    company.setStatus(request.getStatus());
    company.setUpdatedBy(JwtUtils.extractUsernameFromContext());
    String msg = request.getStatus() == 1 ? "Empresa activada" : "Empresa inactivada";
    return new ApiResponse<>(msg, null);
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<CompanyResponse> findMyCompany() {
    Long companyId = TenantContext.getCompanyId();
    return new ApiResponse<>("OK", companyMapper.toResponse(findCompanyOrThrow(companyId)));
  }

  @Override
  @Transactional
  public ApiResponse<CompanyResponse> updateMyCompany(CompanyRequest request,
      MultipartFile logoFile, MultipartFile pfxFile, String pfxPassword) {
    Long companyId = TenantContext.getCompanyId();
    Company company = findCompanyOrThrow(companyId);
    // RUC no puede modificarse desde este endpoint
    request.setRuc(company.getRuc());
    return update(companyId, request, logoFile, pfxFile, pfxPassword);
  }

  // ── Private helpers ───────────────────────────────────────

  private Company findCompanyOrThrow(Long id) {
    return companyRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + id));
  }

  /**
   * Auto-provisions a SYSTEMS profile (is_system=true, all menus) and a default
   * superadmin user for the given company. Called on every new company creation.
   */
  private void provisionSystemsProfile(Company company) {
    Profile systemsProfile = new Profile();
    systemsProfile.setCompany(company);
    systemsProfile.setCode("SYSTEMS");
    systemsProfile.setName("SISTEMAS");
    systemsProfile.setIsSystem(true);
    systemsProfile.setStatus(1);
    systemsProfile.setCreatedBy("system");
    systemsProfile.setMenus(new HashSet<>(menuRepository.findAll()));
    profileRepository.save(systemsProfile);

    DocumentType dniType = documentTypeRepository.findByCode("DNI")
        .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento DNI no encontrado"));

    User systemsUser = new User();
    systemsUser.setCompany(company);
    systemsUser.setProfile(systemsProfile);
    systemsUser.setDocumentType(dniType);
    systemsUser.setDocumentNumber("00000000");
    systemsUser.setFirstName("Super");
    systemsUser.setLastName("Admin");
    systemsUser.setUsername("superadmin");
    systemsUser.setPassword(passwordEncoder.encode(SYSTEMS_DEFAULT_PASSWORD));
    systemsUser.setPlainPassword(SYSTEMS_DEFAULT_PASSWORD);
    systemsUser.setStatus(1);
    systemsUser.setCreatedBy("system");
    userRepository.save(systemsUser);

    log.info("Perfil SISTEMAS y usuario superadmin provisionados para empresa RUC: {}", company.getRuc());
  }

  /**
   * Decodifica el archivo .pfx y extrae la llave pública (certificado) y privada.
   * Ambas se almacenan como Base64 (DER encoded).
   * Solo aplica si pfxFile está presente y no está vacío.
   */
  private void applyPfxIfProvided(Company company, MultipartFile pfxFile, String pfxPassword) {
    if (pfxFile == null || pfxFile.isEmpty()) {
      return;
    }
    if (pfxPassword == null || pfxPassword.isBlank()) {
      throw new BusinessValidationException(
          "Se debe proporcionar la contraseña del certificado .pfx");
    }
    try {
      KeyStore ks = KeyStore.getInstance("PKCS12");
      ks.load(pfxFile.getInputStream(), pfxPassword.toCharArray());

      String alias = null;
      Enumeration<String> aliases = ks.aliases();
      while (aliases.hasMoreElements()) {
        String a = aliases.nextElement();
        if (ks.isKeyEntry(a)) {
          alias = a;
          break;
        }
      }
      if (alias == null) {
        throw new BusinessValidationException(
            "No se encontró una entrada de clave privada en el archivo .pfx");
      }

      PrivateKey privateKey = (PrivateKey) ks.getKey(alias, pfxPassword.toCharArray());
      Certificate certificate = ks.getCertificate(alias);

      company.setSunatCertificatePrivateKey(
          Base64.getEncoder().encodeToString(privateKey.getEncoded()));
      company.setSunatCertificatePublicKey(
          Base64.getEncoder().encodeToString(certificate.getEncoded()));

      log.info("Certificado PFX cargado correctamente para empresa RUC: {}", company.getRuc());

    } catch (BusinessValidationException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error al decodificar certificado PFX", e);
      throw new BusinessValidationException(
          "Error al procesar el certificado .pfx: " + e.getMessage());
    }
  }
}
