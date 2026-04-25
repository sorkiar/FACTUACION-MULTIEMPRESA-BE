package com.api.multiempresa.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Custom exception class for handling login-related errors.
 * This exception is thrown when there are issues during the login process,
 * such as invalid credentials or other authentication failures.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LoginException extends RuntimeException {

  private final Integer statusCode;

  public LoginException(String message, Integer statusCode) {
    super(message);
    this.statusCode = statusCode;
  }
}
