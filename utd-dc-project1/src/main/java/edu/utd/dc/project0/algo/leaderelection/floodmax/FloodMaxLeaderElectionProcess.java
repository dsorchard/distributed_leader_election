package edu.utd.dc.project0.algo.leaderelection.floodmax;

import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.TokenPayload;
import edu.utd.dc.project0.core.Process;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;

public class FloodMaxLeaderElectionProcess extends Process {

  public boolean canStartRound;
  public boolean isTerminated;
  public int maxId;

  public FloodMaxLeaderElectionProcess(ProcessId processId) {
    super(processId);

    this.isTerminated = false;
    this.canStartRound = false;

    this.maxId = processId.getID();
  }

  @Override
  public void init() {
    while (!isTerminated) {

      if (this.canStartRound) nextRound();
      this.canStartRound = false;

      try {
        synchronized (this) {
          wait();
        }
      } catch (InterruptedException ex) {
      }
    }
  }

  public void enableNextRound() {
    this.canStartRound = true;
    synchronized (this) {
      notify();
    }
  }

  public void nextRound() {
    TokenPayload payload = new TokenPayload(maxId);

    for (ProcessId neighbour : neighbours) {
      // TODO: skip sending to parent
      Message message = new Message(processId, payload);
      send(neighbour, message);
    }
  }
}
