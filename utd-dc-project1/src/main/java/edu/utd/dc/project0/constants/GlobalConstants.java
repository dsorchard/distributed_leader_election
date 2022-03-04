package edu.utd.dc.project0.constants;

/** Contains Global Constants and Configuration details. */
public final class GlobalConstants {

  public static final LogLevel LOG_LEVEL = LogLevel.INFO;

  private GlobalConstants() {
    throw new Error("Can't initiate a singleton class");
  }
}
