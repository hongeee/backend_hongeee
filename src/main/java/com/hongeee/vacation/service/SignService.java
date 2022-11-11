package com.hongeee.vacation.service;

import com.hongeee.vacation.api.model.JwtTokenDto;
import com.hongeee.vacation.api.model.RefreshTokenRequestDto;
import com.hongeee.vacation.api.model.SignInRequestDto;
import com.hongeee.vacation.api.model.SignUpRequestDto;
import com.hongeee.vacation.domain.RefreshToken;
import com.hongeee.vacation.domain.User;
import com.hongeee.vacation.exception.RefreshTokenNotFoundException;
import com.hongeee.vacation.exception.UserAlreadyExistException;
import com.hongeee.vacation.exception.UserNotFoundException;
import com.hongeee.vacation.repository.RefreshTokenRepository;
import com.hongeee.vacation.repository.UserRepository;
import com.hongeee.vacation.utils.JwtTokenUtils;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignService {

  private final UserRepository userRepository;

  private final RefreshTokenRepository refreshTokenRepository;

  private final JwtTokenUtils jwtTokenUtils;

  private final PasswordEncoder passwordEncoder;

  @Transactional
  public Long signUp(SignUpRequestDto signUpRequestDto) {
    // 동일 사용자 정보 유무 확인
    if (userRepository.findByEmail(signUpRequestDto.getEmail()).isPresent()) {
      throw new UserAlreadyExistException();
    }

    return userRepository.save(signUpRequestDto.toEntity(passwordEncoder)).getId();
  }

  @Transactional
  public JwtTokenDto signIn(SignInRequestDto signInRequestDto) {
    // 사용자 정보 유무 확인
    User user =
        userRepository
            .findByEmail(signInRequestDto.getEmail())
            .orElseThrow(UserNotFoundException::new);

    // 패스워드 일치 여부 확인
    if (!passwordEncoder.matches(signInRequestDto.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("사용자 정보가 올바르지 않습니다.");
    }

    // AccessToken, RefreshToken 발급
    JwtTokenDto jwtTokenDto = jwtTokenUtils.createJwtToken(user.getId(), user.getRoles());

    RefreshToken refreshToken;
    Optional<RefreshToken> optional = refreshTokenRepository.findByTokenKey(user.getId());

    if (optional.isPresent()) {
      refreshToken = optional.get().updateToken(jwtTokenDto.getRefreshToken());
    } else {
      refreshToken =
          RefreshToken.builder()
              .tokenKey(user.getId())
              .token(jwtTokenDto.getRefreshToken())
              .build();
    }

    refreshTokenRepository.save(refreshToken);

    return jwtTokenDto;
  }

  @Transactional
  public JwtTokenDto refreshJwtToken(RefreshTokenRequestDto refreshTokenRequestDto) {
    // RefreshToken 유효성 체크
    jwtTokenUtils.validationToken(refreshTokenRequestDto.getRefreshToken());

    String accessToken = refreshTokenRequestDto.getAccessToken();
    Authentication authentication = jwtTokenUtils.getAuthentication(accessToken);

    // RefreshToken 일치 여부 확인
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByTokenKey(Long.parseLong(authentication.getName()))
            .orElseThrow(RefreshTokenNotFoundException::new);

    if (!refreshToken.getToken().equals(refreshTokenRequestDto.getRefreshToken())) {
      throw new BadCredentialsException("리프레시 토큰 정보가 올바르지 않습니다.");
    }

    // AccessToken, RefreshToken 재발급
    User user =
        userRepository
            .findById(Long.parseLong(authentication.getName()))
            .orElseThrow(UserNotFoundException::new);

    JwtTokenDto jwtTokenDto = jwtTokenUtils.createJwtToken(user.getId(), user.getRoles());
    refreshTokenRepository.save(refreshToken.updateToken(jwtTokenDto.getRefreshToken()));

    return jwtTokenDto;
  }
}
