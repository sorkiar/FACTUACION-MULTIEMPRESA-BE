package com.api.multiempresa.exception;

import com.api.multiempresa.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the application.
 * This class handles exceptions thrown by the application and returns a standardized response.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
  /**
   * Handles MissingServletRequestParameterException, which occurs when a required request parameter is
   * missing.
   *
   * @param ex the exception that was thrown
   * @return a ResponseEntity containing an ApiResponse with the error message
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Object>> handleMissingParams(
      MissingServletRequestParameterException ex) {
    String errorMessage = String.format("query param '%s' is required and was not provided",
        ex.getParameterName());

    ApiResponse<Object> response = new ApiResponse<>(errorMessage, null);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles all exceptions that are not specifically handled by other methods.
   *
   * @param ex the exception that was thrown
   * @return a ResponseEntity containing an ApiResponse with the error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
    ApiResponse<Object> response = new ApiResponse<>(ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles MethodArgumentNotValidException, which occurs when validation fails.
   *
   * @param ex the exception that was thrown
   * @return a ResponseEntity containing an ApiResponse with the validation error messages
   */
  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
    ApiResponse<Object> response = new ApiResponse<>(
        getOriginClass(ex) + ": " + ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles ResourceNotFoundException, which occurs when a requested resource is not found.
   *
   * @param ex the exception that was thrown
   * @return a ResponseEntity containing an ApiResponse with the error message
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
    ApiResponse<Object> response = new ApiResponse<>(ex.getMessage(), null);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles BusinessValidationException, which occurs when a business validation fails.
   *
   * @param ex the exception that was thrown
   * @return a ResponseEntity containing an ApiResponse with the error message
   */
  @ExceptionHandler(BusinessValidationException.class)
  public ResponseEntity<ApiResponse<Object>> handleBusinessValidationException(
      BusinessValidationException ex) {
    ApiResponse<Object> response = new ApiResponse<>(ex.getMessage(), null);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles LoginException, which occurs during login-related errors.
   * @param ex the exception that was thrown
   * @return a ResponseEntity containing an ApiResponse with the error message
   */
  @ExceptionHandler(LoginException.class)
  public ResponseEntity<ApiResponse<Object>> handleCustomBadRequestException(LoginException ex) {
    ApiResponse<Object> response = new ApiResponse<>(ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatusCode.valueOf(ex.getStatusCode()));
  }

  /**
   * Handles IllegalArgumentException, which occurs when an illegal argument is passed.
   *
   * @param ex the exception that was thrown
   * @return a ResponseEntity containing an ApiResponse with the error message
   */
  private String getOriginClass(Exception ex) {
    StackTraceElement[] stackTrace = ex.getStackTrace();
    if (stackTrace.length > 0) {
      return stackTrace[0].getClassName() + ":" + stackTrace[0].getLineNumber();
    }
    return "-";
  }
}
