package edu.utd.dc.project0.algo.leaderelection.lcr;

import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.TokenPayload;
import edu.utd.dc.project0.algo.leaderelection.fmax.Status;
import edu.utd.dc.project0.core.SyncActor;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;

public class LCRMinLeaderElectionSyncActor extends SyncActor {

  Integer own;
  Integer send;
  Status status;

  public LCRMinLeaderElectionSyncActor(ProcessId processId) {
    super(processId);
  }

  @Override
  public void msgsOutgoing() {}

  @Override
  public void transIncoming() {

  }

  //@Override
  public void transIncoming(Message message) {
    send = null;

    TokenPayload msgPayload = (TokenPayload) (message.payload);

    if (status == Status.LEADER) status = Status.UNKNOWN;
    if (msgPayload.maxId > own) send = msgPayload.maxId;
    else if (msgPayload.maxId == own) {
      status = Status.LEADER;
      send = null;
    }
  }
}
