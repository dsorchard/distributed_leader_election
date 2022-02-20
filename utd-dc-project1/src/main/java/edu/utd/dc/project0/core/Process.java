package edu.utd.dc.project0.core;

import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.core.io.sharedmemory.Listener;
import edu.utd.dc.project0.core.io.sharedmemory.SharedMemoryBus;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;

public class Process implements Runnable, Listener {

  public ProcessId processId;

  public Process(ProcessId processId) {
    this.processId = processId;
  }

  @Override
  public void run() {}

  public void send(ProcessId destinationId, Message message) {
    SharedMemoryBus.send(null, message);
  }

  @Override
  public void onReceive(Message message) {}

  public void addNeighbour(Process neighbourProcess) {
    SharedMemoryBus.register(processId, neighbourProcess.processId, neighbourProcess);
  }
}
