package com.hongeee.vacation.utils;

import com.ibm.icu.util.ChineseCalendar;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class HolidayUtils {

  private static final Map<String, Set<String>> holidaysMap = new HashMap<>();

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

  public String lunarToSolar(String yyyymmdd) {
    ChineseCalendar cc = new ChineseCalendar();

    if (yyyymmdd == null) {
      return null;
    }

    String date = yyyymmdd.trim();

    if (date.length() != 8) {
      if (date.length() == 4) {
        date = date + "0101";
      } else if (date.length() == 6) {
        date = date + "01";
      } else if (date.length() > 8) {
        date = date.substring(0, 8);
      }

      return null;
    }

    cc.set(
        ChineseCalendar.EXTENDED_YEAR,
        Integer.parseInt(date.substring(0, 4)) + 2637); // 년, year + 2637
    cc.set(ChineseCalendar.MONTH, Integer.parseInt(date.substring(4, 6)) - 1); // 월, month -1
    cc.set(ChineseCalendar.DAY_OF_MONTH, Integer.parseInt(date.substring(6))); // 일

    LocalDate solar =
        Instant.ofEpochMilli(cc.getTimeInMillis()).atZone(ZoneId.of("UTC")).toLocalDate();

    int y = solar.getYear();
    int m = solar.getMonth().getValue();
    int d = solar.getDayOfMonth();

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%04d", y));
    sb.append(String.format("%02d", m));
    sb.append(String.format("%02d", d));

    return sb.toString();
  }

  public Set<String> getHolidays(String yyyy) {
    Set<String> holidaysSet = holidaysMap.get(yyyy);

    if (holidaysSet != null) {
      return holidaysSet;
    }

    holidaysSet = new HashSet<String>();
    holidaysMap.put(yyyy, holidaysSet);

    // 양력 휴일
    holidaysSet.add(yyyy + "0101"); // 신정
    holidaysSet.add(yyyy + "0301"); // 3.1절
    holidaysSet.add(yyyy + "0505"); // 어린이날
    holidaysSet.add(yyyy + "0606"); // 현충일
    holidaysSet.add(yyyy + "0815"); // 광복절
    holidaysSet.add(yyyy + "1003"); // 개천절
    holidaysSet.add(yyyy + "1009"); // 한글날
    holidaysSet.add(yyyy + "1225"); // 성탄절

    // 음력 휴일
    String lunarNewYearsEve =
        LocalDate.parse(lunarToSolar(yyyy + "0101"), formatter)
            .minusDays(1)
            .toString()
            .replace("-", "");

    // 설 연휴
    holidaysSet.add(yyyy + lunarNewYearsEve.substring(4));
    holidaysSet.add(yyyy + lunarToSolar(yyyy + "0101").substring(4));
    holidaysSet.add(yyyy + lunarToSolar(yyyy + "0102").substring(4));

    // 석가탄신일
    holidaysSet.add(yyyy + lunarToSolar(yyyy + "0408").substring(4)); // 석가탄신일

    // 추석 연휴
    holidaysSet.add(yyyy + lunarToSolar(yyyy + "0814").substring(4));
    holidaysSet.add(yyyy + lunarToSolar(yyyy + "0815").substring(4));
    holidaysSet.add(yyyy + lunarToSolar(yyyy + "0816").substring(4));

    // 대체공휴일 적용: 설연휴, 삼일절, 어린이날, 광복절, 추석연휴, 개천절, 한글날
    addReplaceHolidaysOfNewYearsDays(yyyy, holidaysSet);
    addReplaceHolidayOf31Day(yyyy, holidaysSet);
    addReplaceHolidayOfChildDay(yyyy, holidaysSet);
    addReplaceHolidayOfLiberationDay(yyyy, holidaysSet);
    addReplaceHolidaysOfThanksGivingDays(yyyy, holidaysSet);
    addReplaceHolidayOfNationalFoundationDay(yyyy, holidaysSet);
    addReplaceHolidayOfHangulDay(yyyy, holidaysSet);

    return holidaysSet;
  }

  /**
   * 설 연휴 대체공휴일
   *
   * @param yyyy
   * @param holidaysSet
   */
  private void addReplaceHolidaysOfNewYearsDays(String yyyy, Set<String> holidaysSet) {
    if (isReplaceHoliday(lunarToSolar(yyyy + "0101"), DayOfWeek.SUNDAY)) {
      holidaysSet.add(lunarToSolar(yyyy + "0103"));
    }

    if (isReplaceHoliday(lunarToSolar(yyyy + "0101"), DayOfWeek.MONDAY)) {
      holidaysSet.add(lunarToSolar(yyyy + "0103"));
    }

    if (isReplaceHoliday(lunarToSolar(yyyy + "0102"), DayOfWeek.SUNDAY)) {
      holidaysSet.add(lunarToSolar(yyyy + "0103"));
    }
  }

  /**
   * 3.1절 대체공휴일
   *
   * @param yyyy
   * @param holidaysSet
   */
  private void addReplaceHolidayOf31Day(String yyyy, Set<String> holidaysSet) {
    if (isReplaceHoliday(yyyy + "0301", DayOfWeek.SUNDAY)) {
      holidaysSet.add(yyyy + "0302");
    }
  }

  /**
   * 어린이날 대체공휴일, 어린이날은 토요일일 경우도 다음 비공휴일을 대체공휴일로 지정
   *
   * @param yyyy
   * @param holidaysSet
   */
  private void addReplaceHolidayOfChildDay(String yyyy, Set<String> holidaysSet) {
    if (isReplaceHoliday(yyyy + "0505", DayOfWeek.SUNDAY)) {
      holidaysSet.add(yyyy + "0506");
    }

    if (isReplaceHoliday(yyyy + "0505", DayOfWeek.SATURDAY)) {
      holidaysSet.add(yyyy + "0507");
    }
  }

  /**
   * 광복절 대체공휴일
   *
   * @param yyyy
   * @param holidaysSet
   */
  private void addReplaceHolidayOfLiberationDay(String yyyy, Set<String> holidaysSet) {
    if (isReplaceHoliday(yyyy + "0815", DayOfWeek.SUNDAY)) {
      holidaysSet.add(yyyy + "0816");
    }
  }

  /**
   * 추석 연휴 대체공휴일
   *
   * @param yyyy
   * @param holidaysSet
   */
  private void addReplaceHolidaysOfThanksGivingDays(String yyyy, Set<String> holidaysSet) {
    if (isReplaceHoliday(lunarToSolar(yyyy + "0814"), DayOfWeek.SUNDAY)) {
      holidaysSet.add(lunarToSolar(yyyy + "0817"));
    }

    if (isReplaceHoliday(lunarToSolar(yyyy + "0815"), DayOfWeek.SUNDAY)) {
      holidaysSet.add(lunarToSolar(yyyy + "0817"));
    }

    if (isReplaceHoliday(lunarToSolar(yyyy + "0816"), DayOfWeek.SUNDAY)) {
      holidaysSet.add(lunarToSolar(yyyy + "0817"));
    }
  }

  /**
   * 개천절 대체공휴일
   *
   * @param yyyy
   * @param holidaysSet
   */
  private void addReplaceHolidayOfNationalFoundationDay(String yyyy, Set<String> holidaysSet) {
    if (isReplaceHoliday(yyyy + "1003", DayOfWeek.SUNDAY)) {
      holidaysSet.add(yyyy + "1004");
    }
  }

  /**
   * 한글날 대체공휴일
   *
   * @param yyyy
   * @param holidaysSet
   */
  private void addReplaceHolidayOfHangulDay(String yyyy, Set<String> holidaysSet) {
    if (isReplaceHoliday(yyyy + "1009", DayOfWeek.SUNDAY)) {
      holidaysSet.add(yyyy + "1010");
    }
  }

  private boolean isReplaceHoliday(String yyyymmdd, DayOfWeek dayOfWeek) {
    if (LocalDate.parse(yyyymmdd, formatter).getDayOfWeek().equals(dayOfWeek)) {
      return true;
    }

    return false;
  }
}
