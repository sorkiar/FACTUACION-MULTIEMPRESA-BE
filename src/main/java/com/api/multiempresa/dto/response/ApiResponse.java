package com.api.multiempresa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response wrapper used for consistent API responses.
 *
 * @param <T> the type of the payload returned in the "data" field
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

  /**
   * Human-readable message about the result of the operation.
   */
  private String message;

  /**
   * Actual data returned by the API, can be any object.
   */
  private T data;
}