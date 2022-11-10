package com.hongeee.vacation.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hongeee.vacation.domain.Vacation;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class VacationRequestDto {

  @JsonFormat(pattern = "yyyy-MM-dd")
  @NotNull
  private LocalDate startDate;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;

  private String comment;

  private String vacationType;

  public Vacation toEntity() {
    return Vacation.builder()
        .startDate(startDate)
        .endDate(endDate)
        .comment(comment)
        .vacationType(VacationType.valueOf(vacationType))
        .vacationStatus(VacationStatus.REQUESTED)
        .build();
  }
}
