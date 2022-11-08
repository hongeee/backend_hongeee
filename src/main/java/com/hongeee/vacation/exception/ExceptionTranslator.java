package com.hongeee.vacation.exception;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblem;

@RestControllerAdvice
public class ExceptionTranslator implements ProblemHandling, SecurityAdviceTrait {

  @Override
  public ResponseEntity<Problem> process(ResponseEntity<Problem> entity, NativeWebRequest request) {
    if (entity == null || entity.getBody() == null) {
      return entity;
    }

    Problem problem = entity.getBody();

    if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
      return entity;
    }

    ProblemBuilder builder =
        Problem.builder()
            .withType(
                Problem.DEFAULT_TYPE.equals(problem.getType())
                    ? ErrorConstants.DEFAULT_TYPE
                    : problem.getType())
            .withStatus(problem.getStatus())
            .withTitle(problem.getTitle())
            .with("path", request.getNativeRequest(HttpServletRequest.class).getRequestURI());

    if (problem instanceof ConstraintViolationProblem) {
      builder
          .with("violations", ((ConstraintViolationProblem) problem).getViolations())
          .with("message", ErrorConstants.ERR_VALIDATION);

      return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
    } else {
      builder
          .withCause(((DefaultProblem) problem).getCause())
          .withDetail(problem.getDetail())
          .withInstance(problem.getInstance());
      problem.getParameters().forEach(builder::with);

      if (!problem.getParameters().containsKey("message") && problem.getStatus() != null) {
        builder.with("message", "error.http." + problem.getStatus().getStatusCode());
      }

      return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
    }
  }

  @Override
  public ResponseEntity<Problem> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, @Nonnull NativeWebRequest request) {
    BindingResult result = ex.getBindingResult();
    List<FieldError> fieldErrors = result.getFieldErrors().stream().collect(Collectors.toList());

    Problem problem =
        Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with("message", ErrorConstants.ERR_VALIDATION)
            .with("fieldErrors", fieldErrors)
            .build();
    return create(ex, problem, request);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Problem> handleRuntimeException(
      RuntimeException ex, NativeWebRequest request) {
    Problem problem =
        Problem.builder()
            .withStatus(Status.INTERNAL_SERVER_ERROR)
            .with("message", ErrorConstants.INTERNAL_SERVER_ERROR)
            .build();
    return create(ex, problem, request);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Problem> handleEntityNotFoundException(
      EntityNotFoundException ex, NativeWebRequest request) {
    Problem problem =
        Problem.builder()
            .withStatus(Status.NOT_FOUND)
            .with("message", ErrorConstants.ENTITY_NOT_FOUND_TYPE)
            .withDetail(ex.getMessage())
            .build();
    return create(ex, problem, request);
  }
}
