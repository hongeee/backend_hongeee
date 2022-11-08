package com.hongeee.vacation.api.model;

import com.hongeee.vacation.domain.User;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public class UserResponseDto {
  private final Long userId;
  private final String email;
  private final String name;
  private List<String> roles;
  private Collection<? extends GrantedAuthority> authorities;
  private final Instant modifiedDate;

  public UserResponseDto(User user) {
    this.userId = user.getUserId();
    this.email = user.getEmail();
    this.name = user.getName();
    this.roles = user.getRoles();
    this.authorities = user.getAuthorities();
    this.modifiedDate = user.getModifiedDate();
  }
}
