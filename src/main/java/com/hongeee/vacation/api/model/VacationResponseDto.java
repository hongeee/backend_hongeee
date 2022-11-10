package com.hongeee.vacation.api.model;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public class VacationResponseDto {

  private Long id;

  private LocalDate startDate;

  private LocalDate endDate;

  private Double period;

  private String comment;

  private String vacationType;

  private String vacationStatus;
}
