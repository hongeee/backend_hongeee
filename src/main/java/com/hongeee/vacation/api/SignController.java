package com.hongeee.vacation.api;

import com.hongeee.vacation.api.model.SignInRequestDto;
import com.hongeee.vacation.api.model.SignUpRequestDto;
import com.hongeee.vacation.api.model.TokenDto;
import com.hongeee.vacation.api.model.TokenRequestDto;
import com.hongeee.vacation.service.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SignController {

  private final SignService signService;

  @PostMapping("/sign-up")
  public ResponseEntity<Long> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
    return ResponseEntity.ok(signService.signUp(signUpRequestDto));
  }

  @PostMapping("/sign-in")
  public ResponseEntity<TokenDto> signIn(@RequestBody SignInRequestDto signInRequestDto) {
    return ResponseEntity.ok(signService.signIn(signInRequestDto));
  }

  @PostMapping("/token/refresh")
  public ResponseEntity<TokenDto> refreshJwtToken(@RequestBody TokenRequestDto tokenRequestDto) {
    return ResponseEntity.ok(signService.refreshJwtToken(tokenRequestDto));
  }
}
