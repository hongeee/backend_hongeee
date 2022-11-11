package com.hongeee.vacation.service;

import com.hongeee.vacation.api.model.AnnualDaysResponseDto;
import com.hongeee.vacation.api.model.VacationRequestDto;
import com.hongeee.vacation.api.model.VacationResponseDto;
import com.hongeee.vacation.api.model.VacationStatus;
import com.hongeee.vacation.api.model.VacationType;
import com.hongeee.vacation.domain.User;
import com.hongeee.vacation.domain.Vacation;
import com.hongeee.vacation.exception.UserNotFoundException;
import com.hongeee.vacation.exception.VacationException;
import com.hongeee.vacation.exception.VacationNotFoundException;
import com.hongeee.vacation.repository.UserRepository;
import com.hongeee.vacation.repository.VacationRepository;
import com.hongeee.vacation.utils.VacationValidationUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VacationService {

  private final UserRepository userRepository;

  private final VacationRepository vacationRepository;

  private final VacationValidationUtils vacationValidationUtils;

  @Transactional(readOnly = true)
  public List<VacationResponseDto> listVacations() {
    User user = getCurrentUser();

    return user.getVacations().stream()
        .map(
            v ->
                VacationResponseDto.builder()
                    .id(v.getId())
                    .startDate(v.getStartDate())
                    .endDate(v.getEndDate())
                    .period(v.getPeriod())
                    .comment(v.getComment())
                    .vacationType(v.getVacationType().name())
                    .vacationStatus(v.getVacationStatus().name())
                    .build())
        .collect(Collectors.toList());
  }

  @Transactional
  public AnnualDaysResponseDto requestVacation(VacationRequestDto vacationRequestDto) {
    User user = getCurrentUser();

    if (user.getAnnualDays() == 0) {
      throw new VacationException("연차를 모두 사용했습니다.");
    }

    // 시작일/종료일 정보를 통해 휴가 사용 일수 계산
    double period = 0.00d;

    switch (VacationType.valueOf(vacationRequestDto.getVacationType())) {
      case DAY:
        period =
            vacationValidationUtils.getPeriodExceptHolidays(
                vacationRequestDto.getStartDate(), vacationRequestDto.getEndDate());
        break;
      case HALF_DAY:
        if (!vacationValidationUtils.isContainedHolidays(vacationRequestDto.getStartDate())) {
          vacationRequestDto.setEndDate(vacationRequestDto.getStartDate());
          period = 0.5d;
        }

        break;
      case QUARTER_DAY:
        if (!vacationValidationUtils.isContainedHolidays(vacationRequestDto.getStartDate())) {
          vacationRequestDto.setEndDate(vacationRequestDto.getStartDate());
          period = 0.25d;
        }

        break;
    }

    if (period == 0) {
      throw new VacationException("휴가 신청 일수가 0일 입니다.");
    }

    // 사용 일수가 남은 휴가 일수보다 적은지 확인
    if (user.getAnnualDays() < period) {
      throw new VacationException("연차가 부족합니다.");
    }

    // 이미 신청한 휴가와 겹치는 날짜가 있는지 확인
    if (user.getVacations().stream()
        .anyMatch(
            v ->
                v.getVacationStatus().equals(VacationStatus.REQUESTED)
                    && ((v.getStartDate().compareTo(vacationRequestDto.getStartDate()) <= 0
                            && v.getEndDate().compareTo(vacationRequestDto.getStartDate()) >= 0)
                        || (v.getStartDate().compareTo(vacationRequestDto.getEndDate()) <= 0
                            && v.getEndDate().compareTo(vacationRequestDto.getEndDate()) >= 0)))) {
      throw new VacationException("중복된 날짜에 휴가 신청이 이미 있습니다.");
    }

    Vacation vacation = vacationRequestDto.toEntity();
    vacation.setPeriod(period);
    vacation.setUser(user);
    vacationRepository.save(vacation);

    user.updateAnnualDays(-vacation.getPeriod());
    userRepository.save(user);

    return AnnualDaysResponseDto.builder().annualDays(user.getAnnualDays()).build();
  }

  @Transactional
  public AnnualDaysResponseDto cancelVacation(Long id) {
    User user = getCurrentUser();

    // 취소하려는 휴가 신청이 사용자가 신청한 것인지 확인
    Vacation vacation =
        user.getVacations().stream()
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElseThrow(VacationNotFoundException::new);

    // 취소하려는 휴가의 시작 날짜 체크
    if (vacation.getStartDate().compareTo(LocalDate.now()) <= 0) {
      throw new VacationException("이미 시작한 휴가는 취소할 수 없습니다.");
    }

    // 휴가 신청 취소 및 연차 일수 반환
    vacation.setVacationStatus(VacationStatus.CANCELED);
    vacationRepository.save(vacation);

    user.updateAnnualDays(vacation.getPeriod());
    userRepository.save(user);

    return AnnualDaysResponseDto.builder().annualDays(user.getAnnualDays()).build();
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new BadCredentialsException("자격 증명이 없습니다.");
    }

    return userRepository
        .findById(Long.parseLong(authentication.getName()))
        .orElseThrow(UserNotFoundException::new);
  }
}
