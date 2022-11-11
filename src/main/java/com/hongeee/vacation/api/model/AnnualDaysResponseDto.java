package com.hongeee.vacation.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public class AnnualDaysResponseDto {

  @JsonProperty private Double annualDays;
}
