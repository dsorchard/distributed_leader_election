package edu.utd.dc.project0.exceptions;

/** Generic Exception class */
public class DCException extends RuntimeException {

  public DCException(String message) {
    super(message);
  }

  public DCException(String message, Throwable cause) {
    super(message, cause);
  }
}
