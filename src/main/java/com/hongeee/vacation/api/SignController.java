package com.hongeee.vacation.api;

import com.hongeee.vacation.api.model.JwtTokenDto;
import com.hongeee.vacation.api.model.RefreshTokenRequestDto;
import com.hongeee.vacation.api.model.SignInRequestDto;
import com.hongeee.vacation.api.model.SignUpRequestDto;
import com.hongeee.vacation.service.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignController {

  private final SignService signService;

  @PostMapping("/sign-up")
  public ResponseEntity<Void> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
    signService.signUp(signUpRequestDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/sign-in")
  public ResponseEntity<JwtTokenDto> signIn(@RequestBody SignInRequestDto signInRequestDto) {
    return ResponseEntity.ok(signService.signIn(signInRequestDto));
  }

  @PostMapping("/token/refresh")
  public ResponseEntity<JwtTokenDto> refreshJwtToken(
      @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
    return ResponseEntity.ok(signService.refreshJwtToken(refreshTokenRequestDto));
  }
}
