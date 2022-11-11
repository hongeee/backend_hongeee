package com.hongeee.vacation.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public class VacationResponseDto {

  @JsonProperty private Long id;

  @JsonProperty private LocalDate startDate;

  @JsonProperty private LocalDate endDate;

  @JsonProperty private Double period;

  @JsonProperty private String comment;

  @JsonProperty private String vacationType;

  @JsonProperty private String vacationStatus;
}
