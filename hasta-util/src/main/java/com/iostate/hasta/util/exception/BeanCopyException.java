package com.iostate.hasta.util.exception;

public class BeanCopyException extends RuntimeException {
  public BeanCopyException(Throwable cause) {
    super(cause);
  }

  public BeanCopyException(String message) {
    super(message);
  }
}
