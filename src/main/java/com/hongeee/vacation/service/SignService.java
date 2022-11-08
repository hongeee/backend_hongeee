package com.hongeee.vacation.service;

import com.hongeee.vacation.api.model.SignInRequestDto;
import com.hongeee.vacation.api.model.SignUpRequestDto;
import com.hongeee.vacation.api.model.TokenDto;
import com.hongeee.vacation.api.model.TokenRequestDto;
import com.hongeee.vacation.config.JwtTokenUtils;
import com.hongeee.vacation.domain.RefreshToken;
import com.hongeee.vacation.domain.User;
import com.hongeee.vacation.repository.RefreshTokenRepository;
import com.hongeee.vacation.repository.UserRepository;
import java.util.Optional;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
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
  public Long signUp(SignUpRequestDto userSignupDto) {
    // 동일 사용자 정보 유무 확인
    if (userRepository.findByEmail(userSignupDto.getEmail()).isPresent()) {
      throw new EntityExistsException("Email is already in use");
    }

    return userRepository.save(userSignupDto.toEntity(passwordEncoder)).getUserId();
  }

  @Transactional
  public TokenDto signIn(SignInRequestDto signInRequestDto) {
    // 사용자 정보 유무 확인
    User user =
        userRepository
            .findByEmail(signInRequestDto.getEmail())
            .orElseThrow(EntityNotFoundException::new);

    // 패스워드 일치 여부 확인
    if (!passwordEncoder.matches(signInRequestDto.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Incorrect account information");
    }

    // AccessToken, RefreshToken 발급
    TokenDto tokenDto = jwtTokenUtils.createJwtToken(user.getUserId(), user.getRoles());

    RefreshToken refreshToken;
    Optional<RefreshToken> optional = refreshTokenRepository.findByTokenKey(user.getUserId());

    if (optional.isPresent()) {
      refreshToken = optional.get().updateToken(tokenDto.getRefreshToken());
    } else {
      refreshToken =
          RefreshToken.builder()
              .tokenKey(user.getUserId())
              .token(tokenDto.getRefreshToken())
              .build();
    }

    refreshTokenRepository.save(refreshToken);

    return tokenDto;
  }

  @Transactional
  public TokenDto refreshJwtToken(TokenRequestDto tokenRequestDto) {
    // RefreshToken 유효성 체크
    jwtTokenUtils.validationToken(tokenRequestDto.getRefreshToken());

    String accessToken = tokenRequestDto.getAccessToken();
    Authentication authentication = jwtTokenUtils.getAuthentication(accessToken);

    // RefreshToken 일치 여부 확인
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByTokenKey(Long.parseLong(authentication.getName()))
            .orElseThrow(EntityNotFoundException::new);

    if (!refreshToken.getToken().equals(tokenRequestDto.getRefreshToken())) {
      throw new BadCredentialsException("Incorrect refresh token");
    }

    // AccessToken, RefreshToken 재발급
    User user =
        userRepository
            .findById(Long.parseLong(authentication.getName()))
            .orElseThrow(EntityNotFoundException::new);

    TokenDto tokenDto = jwtTokenUtils.createJwtToken(user.getUserId(), user.getRoles());
    refreshTokenRepository.save(refreshToken.updateToken(tokenDto.getRefreshToken()));

    return tokenDto;
  }
}
