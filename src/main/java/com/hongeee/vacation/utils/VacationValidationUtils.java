package com.hongeee.vacation.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacationValidationUtils {

  private final HolidayUtils holidayUtils;

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

  public Long getPeriodExceptHolidays(LocalDate startDate, LocalDate endDate) {
    Long period = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    LocalDate tempDate = startDate;

    long count = period;

    for (int i = 0; i < count; i++) {
      if (isContainedHolidays(tempDate)) {
        period--;
      }

      tempDate = tempDate.plusDays(1);
    }

    return period;
  }

  private boolean isContainedHolidays(LocalDate localDate) {
    // 토요일, 일요일일 경우
    if (localDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)
        || localDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
      return true;
    }

    // 공휴일인 경우
    String yyyymmdd = localDate.format(formatter);

    if (holidayUtils.getHolidays(yyyymmdd.substring(0, 4)).contains(yyyymmdd)) {
      return true;
    }

    return false;
  }
}
