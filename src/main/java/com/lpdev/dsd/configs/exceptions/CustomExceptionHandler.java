package com.lpdev.dsd.configs.exceptions;

import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.AUTH;
import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.SERVER;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.responses.BaseOutput;
import java.rmi.ServerException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(value = JwtAuthenticationException.class)
  public ResponseEntity<Object> handleJwtAuthenticationException(JwtAuthenticationException e) {
    log.error("ERROR JwtAuthenticationException: {}", e.getMessage());
    return new ResponseEntity<>(
        BaseOutput.builder().errors(List.of(e.getMessage())).status(ResponseStatus.FAILED).build(),
        HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(value = HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    log.error("ERROR HttpMessageNotReadableException: {}", e.getMessage());
    return new ResponseEntity<>(
        BaseOutput.builder().errors(List.of(e.getMessage())).status(ResponseStatus.FAILED).build(),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    log.error("ERROR MethodArgumentTypeMismatchException: {}", e.getMessage());
    return new ResponseEntity<>(
        BaseOutput.builder().errors(List.of(e.getMessage())).status(ResponseStatus.FAILED).build(),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.error("ERROR MethodArgumentNotValidException: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            BaseOutput.builder()
                .errors(
                    e.getBindingResult().getAllErrors().stream()
                        .map(ObjectError::getDefaultMessage)
                        .toList())
                .status(ResponseStatus.FAILED)
                .build());
  }

  @ExceptionHandler(value = AuthenticationException.class)
  public ResponseEntity<Object> handleAuthenticationException(AuthenticationException e) {
    log.error("ERROR AuthenticationException: {}", e.getMessage());
    return new ResponseEntity<>(
        BaseOutput.builder().errors(List.of(AUTH.FAILED)).status(ResponseStatus.FAILED).build(),
        HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(value = DsdCommonException.class)
  public ResponseEntity<Object> handleDsdFileException(DsdCommonException e) {
    log.error("ERROR DsdCommonException: {}", e.getMessage());
    return new ResponseEntity<>(
        BaseOutput.builder().errors(List.of(e.getMessage())).status(ResponseStatus.FAILED).build(),
        HttpStatus.OK);
  }

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<Object> handleDsdFileException(Exception e) {
    log.error("ERROR Exception: {}", e.getMessage(), e);
    return new ResponseEntity<>(
        BaseOutput.builder().errors(List.of(SERVER.INTERNAL)).status(ResponseStatus.FAILED).build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(value = ServerException.class)
  public ResponseEntity<Object> handleDsdFileException(ServerException e) {
    log.error("ERROR ServerException: {}", e.getMessage());
    return new ResponseEntity<>(
        BaseOutput.builder().errors(List.of(SERVER.INTERNAL)).status(ResponseStatus.FAILED).build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
