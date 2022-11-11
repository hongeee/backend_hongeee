package com.hongeee.vacation.exception;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionTranslator {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorResponse.builder()
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage())
                .build());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(
      HttpServletRequest request, RuntimeException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorResponse.builder()
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage())
                .build());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      HttpServletRequest request, AuthenticationException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            ErrorResponse.builder()
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(e.getMessage())
                .build());
  }

  @ExceptionHandler(RefreshTokenNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleRefreshTokenNotFoundException(
      HttpServletRequest request, RefreshTokenNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            ErrorResponse.builder()
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .status(HttpStatus.NOT_FOUND.value())
                .message("리프레시 토큰 정보가 존재하지 않습니다.")
                .build());
  }

  @ExceptionHandler(UserAlreadyExistException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExistException(
      HttpServletRequest request, UserAlreadyExistException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponse.builder()
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .status(HttpStatus.NOT_FOUND.value())
                .message("이미 존재하는 사용자입니다.")
                .build());
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(
      HttpServletRequest request, UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponse.builder()
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .status(HttpStatus.NOT_FOUND.value())
                .message("사용자 정보가 존재하지 않습니다.")
                .build());
  }

  @ExceptionHandler(VacationException.class)
  public ResponseEntity<ErrorResponse> handleVacationException(
      HttpServletRequest request, VacationException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponse.builder()
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build());
  }

  @ExceptionHandler(VacationNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleVacationNotFoundException(
      HttpServletRequest request, VacationNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            ErrorResponse.builder()
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .status(HttpStatus.NOT_FOUND.value())
                .message("휴가 신청 정보가 존재하지 않습니다.")
                .build());
  }
}
