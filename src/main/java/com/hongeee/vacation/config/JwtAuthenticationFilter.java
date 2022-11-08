package com.hongeee.vacation.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class JwtAuthenticationFilter extends GenericFilterBean {

  private final JwtTokenUtils jwtTokenUtils;

  public JwtAuthenticationFilter(JwtTokenUtils jwtTokenUtils) {
    this.jwtTokenUtils = jwtTokenUtils;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    String token = ((HttpServletRequest) request).getHeader("Authorization");

    // 토큰 검증
    if (token != null && jwtTokenUtils.validationToken(token)) {
      Authentication authentication = jwtTokenUtils.getAuthentication(token);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}
