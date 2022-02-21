package edu.utd.dc.project0.core;

import edu.utd.dc.project0.core.io.sharedmemory.Listener;
import edu.utd.dc.project0.core.io.sharedmemory.SharedMemoryBus;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;

import java.util.ArrayList;
import java.util.List;

public abstract class Process implements Listener, Runnable {
  private final ProcessId processId;
  private final List<ProcessId> neighbours;

  public Process(ProcessId processId) {
    this.neighbours = new ArrayList<>();
    this.processId = processId;
  }

  public void send(ProcessId destinationId, Message message) {
    SharedMemoryBus.send(destinationId, message);
  }

  @Override
  public void run() {
    init();
  }

  @Override
  public void onReceive(Message message) {
    onReceivedMessage(message);
  }

  public void addNeighbour(Process neighbourProcess) {
    this.neighbours.add(neighbourProcess.processId);
    SharedMemoryBus.register(this.processId, neighbourProcess.processId, neighbourProcess);
  }

  public ProcessId getProcessId() {
    return processId;
  }

  public List<ProcessId> getNeighbours() {
    return neighbours;
  }

  public abstract void init();

  public abstract void onReceivedMessage(Message message);
}
