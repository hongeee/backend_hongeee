package com.hongeee.vacation.repository;

import com.hongeee.vacation.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

  Optional<RefreshToken> findByTokenKey(Long tokenKey);
}
