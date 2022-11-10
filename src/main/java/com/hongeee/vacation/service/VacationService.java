package com.hongeee.vacation.service;

import com.hongeee.vacation.api.model.VacationRequestDto;
import com.hongeee.vacation.api.model.VacationResponseDto;
import com.hongeee.vacation.api.model.VacationStatus;
import com.hongeee.vacation.domain.User;
import com.hongeee.vacation.domain.Vacation;
import com.hongeee.vacation.repository.UserRepository;
import com.hongeee.vacation.repository.VacationRepository;
import com.hongeee.vacation.utils.VacationValidationUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
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
  public Double requestVacation(VacationRequestDto vacationRequestDto) {
    User user = getCurrentUser();

    if (user.getAnnualDays() == 0) {
      // TODO: throw Exception
      // 연차를 모두 사용했습니다.
    }

    // 시작일/종료일 정보를 통해 휴가 사용 일수 계산
    Long period = vacationValidationUtils.getPeriodExceptHolidays(
        vacationRequestDto.getStartDate(), vacationRequestDto.getEndDate());

    // 사용 일수가 남은 휴가 일수보다 적은지 확인
    if (user.getAnnualDays() < period) {
      // TODO: throw Exception
      // 남은 연차가 부족합니다.
    }

    // 이미 신청한 휴가와 겹치는 날짜가 있는지 확인

    Vacation vacation = vacationRequestDto.toEntity();
    vacation.setUser(user);
    vacationRepository.save(vacation);
    user.updateAnnualDays(-vacation.getPeriod());
    userRepository.save(user);

    return user.getAnnualDays();
  }

  @Transactional
  public Double cancelVacation(Long id) {
    User user = getCurrentUser();

    // 취소하려는 휴가 신청이 사용자가 신청한 것인지 확인
    Vacation vacation =
        user.getVacations().stream()
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElseThrow(EntityNotFoundException::new);

    // 취소하려는 휴가의 시작 날짜 체크
    if (vacation.getStartDate().isBefore(LocalDate.now())
        || vacation.getStartDate().isEqual(LocalDate.now())) {
      // TODO: 이미 시작한 휴가 취소 불가
    }

    // 휴가 취소 및 연차 일수 반환
    vacation.setVacationStatus(VacationStatus.CANCELED);
    vacationRepository.save(vacation);
    user.updateAnnualDays(vacation.getPeriod());
    userRepository.save(user);

    return user.getAnnualDays();
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new BadCredentialsException("No credential");
    }

    return userRepository
        .findById(Long.parseLong(authentication.getName()))
        .orElseThrow(EntityNotFoundException::new);
  }
}
