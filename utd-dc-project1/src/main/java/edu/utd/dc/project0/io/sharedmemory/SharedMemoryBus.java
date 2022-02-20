package edu.utd.dc.project0.io.sharedmemory;

import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.io.sharedmemory.domain.Message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SharedMemoryBus {

  public static Map<ProcessId, List<Destination>> list = new LinkedHashMap<>();

  public static void register(ProcessId source, ProcessId destination, Observer observer) {
    list.putIfAbsent(source, new ArrayList<>());
    list.get(source).add(new Destination(destination, observer));
  }

  public static void send(ProcessId destinationId, Message message) {
    ProcessId sourceId = message.source;

    for (Destination destination : list.get(sourceId))
      if (destination.destinationId == destinationId) destination.client.onReceive(message);
  }

  static class Destination {
    ProcessId destinationId;
    Observer client;

    public Destination(ProcessId destinationId, Observer client) {
      this.destinationId = destinationId;
      this.client = client;
    }
  }
}
