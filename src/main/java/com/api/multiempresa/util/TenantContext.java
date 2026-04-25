package com.api.multiempresa.util;

public class TenantContext {

  private static final ThreadLocal<Long> CURRENT_COMPANY = new ThreadLocal<>();

  public static void setCurrentCompanyId(Long companyId) {
    CURRENT_COMPANY.set(companyId);
  }

  public static Long getCurrentCompanyId() {
    return CURRENT_COMPANY.get();
  }

  /** Alias for getCurrentCompanyId(). */
  public static Long getCompanyId() {
    return CURRENT_COMPANY.get();
  }

  public static void clear() {
    CURRENT_COMPANY.remove();
  }
}
