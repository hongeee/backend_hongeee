package com.hongeee.vacation.api.model;

import com.hongeee.vacation.domain.User;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestDto {
  private String email;
  private String password;
  private String name;
  private String nickName;

  public User toEntity(PasswordEncoder passwordEncoder) {
    return User.builder()
        .email(email)
        .password(passwordEncoder.encode(password))
        .name(name)
        .roles(Collections.singletonList("ROLE_USER"))
        .build();
  }
}
