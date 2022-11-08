package com.hongeee.vacation.config;

import com.hongeee.vacation.api.model.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.Base64UrlCodec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenUtils {

  @Value("spring.jwt.secret")
  private String secretKey;

  private final UserDetailsService userDetailsService;

  private static final String ROLES = "roles";
  private static final Long ACCESS_TOKEN_VALID_MS = 30 * 60 * 1000L;  // 30 minute
  private static final Long REFRESH_TOKEN_VALID_MS = 24 * 60 * 60 * 1000L; // 1 day

  @PostConstruct
  protected void init() {
    // BASE64 Encode
    secretKey = Base64UrlCodec.BASE64URL.encode(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public TokenDto createJwtToken(Long userPk, List<String> roles) {
    Claims claims = Jwts.claims().setSubject(String.valueOf(userPk));
    claims.put(ROLES, roles);

    Date now = new Date();

    // AccessToken
    String accessToken =
        Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_MS))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();

    // RefreshToken
    String refreshToken =
        Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_MS))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();

    return TokenDto.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpireDate(ACCESS_TOKEN_VALID_MS)
        .build();
  }

  public Authentication getAuthentication(String accessToken) {
    Claims claims = parseClaims(accessToken);

    if (claims.get(ROLES) == null) {
      throw new BadCredentialsException("Invalid token");
    }

    // 사용자 정보 조회
    UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  private Claims parseClaims(String token) {
    try {
      return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  public boolean validationToken(String token) {
    try {
      Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

      return true;
    } catch (SecurityException | MalformedJwtException e) {
      throw new BadCredentialsException("Invalid signature");
    } catch (ExpiredJwtException e) {
      throw new BadCredentialsException("Expired token");
    } catch (UnsupportedJwtException e) {
      throw new BadCredentialsException("Unsupported token");
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException("Invalid token");
    }
  }
}
