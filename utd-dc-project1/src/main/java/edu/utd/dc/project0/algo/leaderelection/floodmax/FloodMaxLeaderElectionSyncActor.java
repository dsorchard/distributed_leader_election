package edu.utd.dc.project0.algo.leaderelection.floodmax;

import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.IAmDonePayload;
import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.RejectPayload;
import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.TerminatePayload;
import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.TokenPayload;
import edu.utd.dc.project0.constants.LogLevel;
import edu.utd.dc.project0.core.SyncActor;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.tree.TreeNode;

import java.util.HashSet;
import java.util.Set;

public class FloodMaxLeaderElectionSyncActor extends SyncActor {

  private final TreeNode<ProcessId> bfsTree;
  private final Set<ProcessId> rejectProcessIdSet;
  private final Set<ProcessId> iAmDoneProcessIdSet;
  private int leaderId;

  public FloodMaxLeaderElectionSyncActor(ProcessId processId) {
    super(processId);

    this.leaderId = processId.getID();
    this.bfsTree = new TreeNode<>();

    this.rejectProcessIdSet = new HashSet<>();
    this.iAmDoneProcessIdSet = new HashSet<>();
  }

  @Override
  protected void handleNextRound(int roundNumber) {
    System.out.println(getProcessId() + " Round # " + roundNumber);
    this.rejectProcessIdSet.clear();
    this.iAmDoneProcessIdSet.clear();
  }

  @Override
  public void handleIncoming() {
    this.prevRoundReceivedMessages.forEach(
        message -> {
          log(LogLevel.DEBUG, "Receiver " + getProcessId() + " " + message.toString());

          if (message.payload instanceof TokenPayload) {
            handleTokenMessage(message._source, (TokenPayload) message.payload);
          } else if (message.payload instanceof RejectPayload) {
            handleRejectMessage(message._source);
          } else if (message.payload instanceof IAmDonePayload) {
            handleIAmDoneMessage(message._source);
          } else if (message.payload instanceof TerminatePayload) {
            handleTerminateMessage(message._source, (TerminatePayload) message.payload);
          }
        });
  }

  @Override
  public void handleOutgoing() {

    getNeighbours()
        .forEach(
            neighbour -> send(neighbour, new Message(getProcessId(), new TokenPayload(leaderId))));
  }

  private synchronized void handleTokenMessage(ProcessId source, TokenPayload payload) {
    if (payload.leaderId > leaderId) {
      // 1. set max ID
      leaderId = payload.leaderId;

      // 2. set tree relation
      bfsTree.isLeaf = false;
      bfsTree.parentId = source;

    } else {
      send(source, new Message(getProcessId(), new RejectPayload()));
    }
  }

  private synchronized void handleRejectMessage(ProcessId source) {
    this.rejectProcessIdSet.add(source);

    if (this.rejectProcessIdSet.size() == getNeighbours().size()) {
      bfsTree.isLeaf = true;
      send(bfsTree.parentId, new Message(getProcessId(), new IAmDonePayload()));
    }
  }

  private synchronized void handleIAmDoneMessage(ProcessId source) {
    this.iAmDoneProcessIdSet.add(source);
    this.bfsTree.children.add(source);

    if (this.iAmDoneProcessIdSet.size() == getNeighbours().size()) {
      if (bfsTree.parentId == null) terminate();
      else send(bfsTree.parentId, new Message(getProcessId(), new IAmDonePayload()));
    }
  }

  private synchronized void handleTerminateMessage(ProcessId source, TerminatePayload payload) {
    this.leaderId = payload.leaderId;
    terminate();
  }

  private void terminate() {
    setTerminated(true);
    bfsTree.children.forEach(
        child -> send(child, new Message(getProcessId(), new TerminatePayload(leaderId))));
  }

  public int getLeaderId() {
    return leaderId;
  }
}
