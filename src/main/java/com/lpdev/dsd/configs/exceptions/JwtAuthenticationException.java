package com.lpdev.dsd.configs.exceptions;

public class JwtAuthenticationException extends RuntimeException {
  public JwtAuthenticationException(String msg) {
    super(msg);
  }
}
