package edu.utd.dc.async.core.io.sharedmemory;

import edu.utd.dc.common.design_pattern.listener.Listener;
import edu.utd.dc.common.domain.io.Message;
import edu.utd.dc.common.domain.support.ProcessId;
import edu.utd.dc.common.telemetry.MessageTelemetryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/** Singleton class responsible for handling messages in the network. */
public class SharedMemoryBus {

  // Used for maintaining the current timestamp (relative) w.r.t channel.
  static int scalarClock = 1;

  // Using Blocking Priority Queue, as we will have multiple concurrent inserts.
  static PriorityBlockingQueue<DelayedMessage> pq = new PriorityBlockingQueue<>();
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
    // Used for logging message metrics. Better use AOP here.
    MessageTelemetryUtils.log(message.payload.getClass());

    ProcessId sourceId = message._source;

    for (Destination destination : map.get(sourceId))
      if (destination.destinationId == destinationId) destination.client.onReceive(message);
  }

  /**
   * This function inserts message to the PQ rather than sending it directly. The PQ then introduces
   * a notion of delay by continous polling to simulate partially synchronous system.
   */
  public static synchronized void send(ProcessId destinationId, Message message, int delay) {
    pq.add(new DelayedMessage(destinationId, message, scalarClock + delay));
  }

  /** The function invoked periodically from the driver. This ensure partial synchrony. */
  public static void tick() {
    scalarClock++;
    onTick();
  }

  /**
   * Upon a new tick, we will loop through the priority queue and check the topmost elements and see
   * if the timestamp matches.
   */
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

  /**
   * Delayed Message wrapper is used to wrap the original message and introduce a delay. This
   * message when it is inserted into the PriorityQueue will give a notion of delay using continous
   * periodic polling and checking agains the exitTs (exit Timestamp) value.
   */
  public static class DelayedMessage implements Comparable<DelayedMessage> {
    ProcessId destinationId;
    Message message;
    public Integer exitTs;

    public DelayedMessage(ProcessId destinationId, Message message, int exitTs) {
      this.destinationId = destinationId;
      this.message = message;
      this.exitTs = exitTs;
    }

    @Override
    public int compareTo(DelayedMessage msg) {
      return this.exitTs.compareTo(msg.exitTs);
    }
  }
}
