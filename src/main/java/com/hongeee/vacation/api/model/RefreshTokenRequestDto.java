package com.hongeee.vacation.api.model;

import lombok.Getter;

@Getter
public class RefreshTokenRequestDto {

  private String accessToken;

  private String refreshToken;
}
