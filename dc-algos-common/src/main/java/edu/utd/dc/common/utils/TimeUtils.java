package edu.utd.dc.common.utils;

import java.util.Random;

/** Utility class for Time related functions. */
public final class TimeUtils {

  public static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  public static void randomSleepBetween(int min, int max) {
    Random rand = new Random();
    int randTimeInMills = rand.nextInt((max - min) + 1) + min;

    try {
      Thread.sleep(randTimeInMills);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}
