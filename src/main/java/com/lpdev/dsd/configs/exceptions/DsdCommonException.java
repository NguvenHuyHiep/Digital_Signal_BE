package com.lpdev.dsd.configs.exceptions;

public class DsdCommonException extends RuntimeException {
  public DsdCommonException(String msg) {
    super(msg);
  }

  public DsdCommonException(Exception e) {
    super(e);
  }

  public DsdCommonException(String msg, Exception e) {
    super(msg, e);
  }
}
