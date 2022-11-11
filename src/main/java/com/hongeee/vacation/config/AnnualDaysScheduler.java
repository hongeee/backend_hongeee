package com.hongeee.vacation.config;

import com.hongeee.vacation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AnnualDaysScheduler {

  private final UserRepository userRepository;

  @Scheduled(cron = "0 0 0 1 1 ? *")
  @Transactional
  public void initAnnualDays() {
    userRepository.updateAnnualDays(15d);
  }
}
