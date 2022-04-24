package edu.utd.dc.async.bfs.layered.telemetry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class MessageTelemetryUtils {
  private static final Map<String, AtomicInteger> map = new ConcurrentHashMap<>();

  public static synchronized void log(Class clazz) {
    map.putIfAbsent(clazz.getSimpleName(), new AtomicInteger());
    map.get(clazz.getSimpleName()).incrementAndGet();
  }

  public static void print() {
    System.out.println(map);
  }
}
