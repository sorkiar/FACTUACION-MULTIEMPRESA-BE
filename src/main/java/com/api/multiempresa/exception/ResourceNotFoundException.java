package com.api.multiempresa.exception;

/**
 * Exception thrown when a requested resource is not found.
 * This is a runtime exception that can be used to indicate
 * that a specific resource (like a candidate or employee)
 * could not be located in the system.
 */
public class ResourceNotFoundException extends RuntimeException {
  /**
   * Constructs a new ResourceNotFoundException with the specified detail message.
   *
   * @param message the detail message, which is saved for later retrieval by the
   *                {@link Throwable#getMessage()} method.
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
