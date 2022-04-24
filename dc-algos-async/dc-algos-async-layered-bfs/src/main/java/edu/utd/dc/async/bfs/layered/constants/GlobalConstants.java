package edu.utd.dc.async.bfs.layered.constants;

import edu.utd.dc.common.constants.LogLevel;

/** Contains Global Constants and Configuration details. */
public final class GlobalConstants {

  public static final LogLevel LOG_LEVEL = LogLevel.INFO;

  private GlobalConstants() {
    throw new Error("Can't initiate a singleton class");
  }
}
