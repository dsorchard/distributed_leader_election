package edu.utd.dc.project0.core;

import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.io.sharedmemory.Listener;
import edu.utd.dc.project0.io.sharedmemory.SharedMemoryBus;
import edu.utd.dc.project0.io.sharedmemory.domain.Message;

public class Process implements Runnable, Listener {

  public ProcessId processId;
  public boolean canStartRound;
  public boolean isTerminated;

  public Process(ProcessId processId) {
    this.processId = processId;

    this.canStartRound = false;
    this.isTerminated = false;
  }

  @Override
  public void run() {

    while (!isTerminated) {
      if (canStartRound) nextRound();
      canStartRound = false;
    }
  }

  private void nextRound() {}

  public void send(ProcessId destinationId, Message message) {
    SharedMemoryBus.send(null, message);
  }

  @Override
  public void onReceive(Message message) {}

  public void addNeighbour(Process neighbourProcess) {
    SharedMemoryBus.register(processId, neighbourProcess.processId, neighbourProcess);
  }

  public void setCanStartRound(boolean canStartRound) {
    this.canStartRound = canStartRound;
  }
}
