package edu.utd.dc.project0.algo.leaderelection.floodmax;

import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.IAmDonePayload;
import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.RejectPayload;
import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.TerminatePayload;
import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.TokenPayload;
import edu.utd.dc.project0.constants.GlobalConstants;
import edu.utd.dc.project0.constants.LogLevel;
import edu.utd.dc.project0.core.Process;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.tree.TreeNode;

public class FloodMaxLeaderElectionProcess extends Process {

  private final TreeNode<ProcessId> bfsTree;

  private boolean canStartRound;
  private boolean isTerminated;

  private int maxId;
  private int rejectCount;
  private int iAmDoneCount;

  public FloodMaxLeaderElectionProcess(ProcessId processId) {
    super(processId);

    this.isTerminated = false;
    this.canStartRound = false;

    this.bfsTree = new TreeNode<>();
    this.maxId = processId.getID();
  }

  @Override
  public void init() {
    while (!isTerminated) {

      if (this.canStartRound) nextRound();
      this.canStartRound = false;

      syncWait();
    }
  }

  @Override
  public void onReceivedMessage(Message message) {
    log(LogLevel.DEBUG, message._source.getID() + " " + message.data.toString());

    if (message.data instanceof TokenPayload) {
      onReceiveTokenMessage(message._source, (TokenPayload) message.data);
    } else if (message.data instanceof RejectPayload) {
      onReceiveRejectMessage(message._source, (RejectPayload) message.data);
    } else if (message.data instanceof IAmDonePayload) {
      onReceiveIAmDoneMessage(message._source, (IAmDonePayload) message.data);
    } else if (message.data instanceof TerminatePayload) {
      onReceiveTerminateMessage(message._source, (TerminatePayload) message.data);
    }
  }

  private synchronized void onReceiveTokenMessage(ProcessId source, TokenPayload payload) {
    if (payload.maxId > maxId) {
      // 1. set max ID
      maxId = payload.maxId;

      // 2. set tree relation
      bfsTree.isLeaf = false;
      bfsTree.parentId = source;

    } else {
      send(source, new Message(getProcessId(), new RejectPayload()));
    }
  }

  private synchronized void onReceiveRejectMessage(ProcessId source, RejectPayload payload) {
    this.rejectCount++;

    if (this.rejectCount == getNeighbours().size()) {
      bfsTree.isLeaf = true;
      send(bfsTree.parentId, new Message(getProcessId(), new IAmDonePayload(maxId)));
    }
  }

  private synchronized void onReceiveIAmDoneMessage(ProcessId source, IAmDonePayload payload) {
    this.iAmDoneCount++;

    this.maxId = Math.max(maxId, payload.maxId);

    // I am done is sent by its children
    this.bfsTree.children.add(source);

    if (this.iAmDoneCount == getNeighbours().size()) terminate();
  }

  private synchronized void onReceiveTerminateMessage(ProcessId source, TerminatePayload payload) {
    isTerminated = true;

    if (!bfsTree.isLeaf)
      bfsTree.children.forEach(child -> send(child, new Message(getProcessId(), payload)));
  }

  private void terminate() {
    isTerminated = true;
    bfsTree.children.forEach(
        child -> send(child, new Message(getProcessId(), new TerminatePayload(maxId))));
  }

  public void enableNextRound() {
    this.canStartRound = true;
    syncNotify();
  }

  public void nextRound() {
    TokenPayload payload = new TokenPayload(maxId);

    for (ProcessId neighbour : getNeighbours()) {
      // TODO: skip sending to parent
      Message message = new Message(getProcessId(), payload);
      send(neighbour, message);
    }
  }

  private void syncWait() {
    try {
      synchronized (this) {
        wait();
      }
    } catch (InterruptedException ex) {
    }
  }

  private void syncNotify() {
    synchronized (this) {
      notify();
    }
  }

  private void log(LogLevel logLevel, String message) {
    if (logLevel.getValue() >= GlobalConstants.LOG_LEVEL.getValue()) System.out.println(message);
  }
}
