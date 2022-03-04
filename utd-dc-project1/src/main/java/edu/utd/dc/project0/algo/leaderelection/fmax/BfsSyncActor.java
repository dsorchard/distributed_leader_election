package edu.utd.dc.project0.algo.leaderelection.fmax;

import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.TokenPayload;
import edu.utd.dc.project0.core.SyncActor;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.tree.TreeNode;

public class BfsSyncActor extends SyncActor {
  private final TreeNode<ProcessId> bfsTree;

  public BfsSyncActor(ProcessId processId) {
    super(processId);

    this.bfsTree = new TreeNode<>();
  }

  @Override
  public void msgsOutgoing() {
    TokenPayload payload = new TokenPayload(maxId);

    for (ProcessId neighbour : getNeighbours()) {
      Message message = new Message(getProcessId(), payload);
      send(neighbour, message);
    }
  }

  @Override
  public void transIncoming() {
    this.messageQueue.forEach(message -> {

    });
  }
}
