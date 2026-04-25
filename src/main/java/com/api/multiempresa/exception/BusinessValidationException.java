package com.api.multiempresa.exception;

/**
 * Exception thrown when a business validation fails.
 * This is a runtime exception that can be used to indicate
 * that a specific business rule or validation has not been met.
 */
public class BusinessValidationException extends RuntimeException {
  /**
   * Constructs a new BusinessValidationException with the specified detail message.
   *
   * @param message the detail message, which is saved for later retrieval by the
   *                {@link Throwable#getMessage()} method.
   */
  public BusinessValidationException(String message) {
    super(message);
  }
}
