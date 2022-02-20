package edu.utd.dc.project0.core;

import edu.utd.dc.project0.algo.RbaAlgo;
import edu.utd.dc.project0.core.io.sharedmemory.Listener;
import edu.utd.dc.project0.core.io.sharedmemory.SharedMemoryBus;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;

public class Process implements Runnable, Listener {

  public ProcessId processId;
  public RbaAlgo algo;

  public Process(ProcessId processId, RbaAlgo algo) {
    this.processId = processId;
    this.algo = algo;
  }

  @Override
  public void run() {
    this.algo.init();
  }

  public void send(ProcessId destinationId, Message message) {
    SharedMemoryBus.send(destinationId, message);
  }

  @Override
  public void onReceive(Message message) {}

  public void addNeighbour(Process neighbourProcess) {
    SharedMemoryBus.register(this.processId, neighbourProcess.processId, neighbourProcess);
  }
}
