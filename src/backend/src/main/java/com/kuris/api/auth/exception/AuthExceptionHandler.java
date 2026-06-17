package com.kuris.api.auth.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Mapea excepciones de auth a respuestas JSON con campo `error`. */
@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ProblemDetail handle(ResponseStatusException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(ex.getStatusCode());
    detail.setProperty("error", ex.getReason());
    return detail;
  }
}
