package edu.utd.dc.asynch.utils;

import java.util.Random;

public class RandomUtils {

  public static int valueBetween(int min, int max) {
    //    return 1;
    Random random = new Random();
    return random.nextInt(max + min) + min;
  }
}
