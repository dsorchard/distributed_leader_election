package edu.utd.dc.project2.core.io.sharedmemory;

import edu.utd.dc.project2.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project2.core.support.ProcessId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Singleton class responsible for handling messages in the network. */
public final class SharedMemoryBus {

  static int scalarClock = 1;

  static PriorityQueue<DelayedMessage> pq =
      new PriorityQueue<>(Comparator.comparingInt(a -> a.exitTs));
  public static Map<ProcessId, List<Destination>> map = new ConcurrentHashMap<>();

  /**
   * Registers an edge.
   *
   * @param source contains start of an edge
   * @param destination contains end of an edge
   * @param listener contains the destination client
   */
  public static void register(ProcessId source, ProcessId destination, Listener listener) {
    map.putIfAbsent(source, new ArrayList<>());
    map.get(source).add(new Destination(destination, listener));
  }

  /**
   * Responsible for sending message. Since we are using Listener Pattern, the message will be
   * delivered immediately as we are invoking the onReceive() function.
   *
   * @param destinationId Destination ProcessId
   * @param message Message to be delivered
   */
  public static synchronized void send(ProcessId destinationId, Message message) {
    ProcessId sourceId = message._source;

    for (Destination destination : map.get(sourceId))
      if (destination.destinationId == destinationId) destination.client.onReceive(message);
  }

  public static synchronized void send(ProcessId destinationId, Message message, int delay) {
    pq.add(new DelayedMessage(destinationId, message, scalarClock + delay));
  }

  public static void tick() {
    scalarClock++;
    onTick();
  }

  private static void onTick() {
    while (!pq.isEmpty() && scalarClock >= pq.peek().exitTs) {
      send(pq.peek().destinationId, pq.peek().message);
      pq.poll();
    }
  }

  /** Contains the Destination details */
  static class Destination {
    ProcessId destinationId;
    Listener client;

    public Destination(ProcessId destinationId, Listener client) {
      this.destinationId = destinationId;
      this.client = client;
    }
  }

  public static class DelayedMessage {
    ProcessId destinationId;
    Message message;
    int exitTs;

    public DelayedMessage(ProcessId destinationId, Message message, int exitTs) {
      this.destinationId = destinationId;
      this.message = message;
      this.exitTs = exitTs;
    }
  }
}
