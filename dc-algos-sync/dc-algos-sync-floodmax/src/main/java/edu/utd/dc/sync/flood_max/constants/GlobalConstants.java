package edu.utd.dc.sync.flood_max.constants;

import edu.utd.dc.common.constants.LogLevel;

/** Contains Global Constants and Configuration details. */
public final class GlobalConstants {

  public static final LogLevel LOG_LEVEL = LogLevel.DEBUG;

  private GlobalConstants() {
    throw new Error("Can't initiate a singleton class");
  }
}
