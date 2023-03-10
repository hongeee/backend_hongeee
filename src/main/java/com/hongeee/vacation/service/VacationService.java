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
      throw new VacationException("????????? ?????? ??????????????????.");
    }

    // ?????????/????????? ????????? ?????? ?????? ?????? ?????? ??????
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
      throw new VacationException("?????? ?????? ????????? 0??? ?????????.");
    }

    // ?????? ????????? ?????? ?????? ???????????? ????????? ??????
    if (user.getAnnualDays() < period) {
      throw new VacationException("????????? ???????????????.");
    }

    // ?????? ????????? ????????? ????????? ????????? ????????? ??????
    if (user.getVacations().stream()
        .anyMatch(
            v ->
                v.getVacationStatus().equals(VacationStatus.REQUESTED)
                    && ((v.getStartDate().compareTo(vacationRequestDto.getStartDate()) <= 0
                            && v.getEndDate().compareTo(vacationRequestDto.getStartDate()) >= 0)
                        || (v.getStartDate().compareTo(vacationRequestDto.getEndDate()) <= 0
                            && v.getEndDate().compareTo(vacationRequestDto.getEndDate()) >= 0)))) {
      throw new VacationException("????????? ????????? ?????? ????????? ?????? ????????????.");
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

    // ??????????????? ?????? ????????? ???????????? ????????? ????????? ??????
    Vacation vacation =
        user.getVacations().stream()
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElseThrow(VacationNotFoundException::new);

    // ??????????????? ????????? ?????? ?????? ??????
    if (vacation.getStartDate().compareTo(LocalDate.now()) <= 0) {
      throw new VacationException("?????? ????????? ????????? ????????? ??? ????????????.");
    }

    // ?????? ?????? ?????? ??? ?????? ?????? ??????
    vacation.setVacationStatus(VacationStatus.CANCELED);
    vacationRepository.save(vacation);

    user.updateAnnualDays(vacation.getPeriod());
    userRepository.save(user);

    return AnnualDaysResponseDto.builder().annualDays(user.getAnnualDays()).build();
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new BadCredentialsException("?????? ????????? ????????????.");
    }

    return userRepository
        .findById(Long.parseLong(authentication.getName()))
        .orElseThrow(UserNotFoundException::new);
  }
}
