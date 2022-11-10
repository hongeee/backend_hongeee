package com.hongeee.vacation.api;

import com.hongeee.vacation.api.model.VacationRequestDto;
import com.hongeee.vacation.api.model.VacationResponseDto;
import com.hongeee.vacation.service.VacationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class VacationController {

  private final VacationService vacationService;

  @GetMapping("/vacations")
  public ResponseEntity<List<VacationResponseDto>> listVacation() {
    return ResponseEntity.ok(vacationService.listVacations());
  }

  @PostMapping("/vacations")
  public ResponseEntity<Double> requestVacation(
      @RequestBody VacationRequestDto vacationRequestDto) {
    return ResponseEntity.ok(vacationService.requestVacation(vacationRequestDto));
  }

  @PutMapping("/vacations/{id}")
  public ResponseEntity<Double> cancelVacation(@PathVariable("id") Long id) {
    return ResponseEntity.ok(vacationService.cancelVacation(id));
  }
}
