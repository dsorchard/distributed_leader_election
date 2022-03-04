package edu.utd.dc.project0.algo.leaderelection.fmax;

import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.TokenPayload;
import edu.utd.dc.project0.core.SyncActor;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;

public class FloodMaxLeaderElectionSyncActor extends SyncActor {
  Integer own;
  Integer maxId;
  Status status;

  int roundNumber;
  int diameter = 10;

  public FloodMaxLeaderElectionSyncActor(ProcessId own) {
    super(own);
  }

  @Override
  public void msgsOutgoing() {
    if (roundNumber < diameter) {
      TokenPayload payload = new TokenPayload(maxId);

      for (ProcessId neighbour : getNeighbours()) {
        Message message = new Message(getProcessId(), payload);
        send(neighbour, message);
      }
    }
  }

  @Override
  public void transIncoming() {
    roundNumber++;

    for (Message message : this.messageQueue) maxId = Math.max(maxId, (int) message.payload);

    if (roundNumber == diameter) {
      if (maxId == own) status = Status.LEADER;
      else status = Status.NON_LEADER;
    }
  }
}
