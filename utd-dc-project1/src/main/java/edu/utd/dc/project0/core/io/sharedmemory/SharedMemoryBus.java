package edu.utd.dc.project0.core.io.sharedmemory;

import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SharedMemoryBus {

  public static Map<ProcessId, List<Destination>> list = new ConcurrentHashMap<>();

  public static void register(ProcessId source, ProcessId destination, Listener listener) {
    list.putIfAbsent(source, new ArrayList<>());
    list.get(source).add(new Destination(destination, listener));
  }

  public static synchronized void send(ProcessId destinationId, Message message) {
    ProcessId sourceId = message._source;

    for (Destination destination : list.get(sourceId))
      if (destination.destinationId == destinationId) destination.client.onReceive(message);
  }

  static class Destination {
    ProcessId destinationId;
    Listener client;

    public Destination(ProcessId destinationId, Listener client) {
      this.destinationId = destinationId;
      this.client = client;
    }
  }
}
