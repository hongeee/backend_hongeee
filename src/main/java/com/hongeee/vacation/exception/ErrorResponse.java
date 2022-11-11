package com.hongeee.vacation.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public class ErrorResponse {

  @JsonProperty
  private String error;

  @JsonProperty
  private int status;

  @JsonProperty
  private String message;
}
