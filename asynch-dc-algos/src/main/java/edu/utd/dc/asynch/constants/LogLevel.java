package edu.utd.dc.asynch.constants;

/** Contains log level for the sysout. */
public enum LogLevel {
  TRACE(0),
  DEBUG(1),
  INFO(2);

  public final int level;

  LogLevel(int level) {
    this.level = level;
  }

  public int getValue() {
    return level;
  }
}
