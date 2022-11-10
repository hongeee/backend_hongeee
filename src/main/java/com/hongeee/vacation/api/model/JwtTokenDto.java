package com.hongeee.vacation.api.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtTokenDto {

  private String grantType;

  private String accessToken;

  private String refreshToken;

  private Long accessTokenExpireDate;
}
