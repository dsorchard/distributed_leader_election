package edu.utd.dc.project0.algo.leaderelection.floodmax;

import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.IAmDonePayload;
import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.RejectPayload;
import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.TerminatePayload;
import edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload.TokenPayload;
import edu.utd.dc.project0.constants.GlobalConstants;
import edu.utd.dc.project0.constants.LogLevel;
import edu.utd.dc.project0.core.SyncActor;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;
import edu.utd.dc.project0.tree.TreeNode;

/**
 * Contains the core algorithm related implementations.
 *
 * <p>Extends process class to inherit properties like onReceive(), send(), getNeighbours() etc.
 */
public class FloodMaxLeaderElectionSyncActor extends SyncActor {

  private final TreeNode<ProcessId> bfsTree;

  private int maxId;
  private int rejectCount;
  private int iAmDoneCount;

  public FloodMaxLeaderElectionSyncActor(ProcessId processId) {
    super(processId);

    this.bfsTree = new TreeNode<>();
    this.maxId = processId.getID();
  }

  public void msgsOutgoing() {
    TokenPayload payload = new TokenPayload(maxId);

    for (ProcessId neighbour : getNeighbours()) {
      // TODO: skip sending to parent
      Message message = new Message(getProcessId(), payload);
      send(neighbour, message);
    }
  }

  @Override
  public void transIncoming() {

  }

  /**
   * Parent function which recieve all the incoming messages. Based on the message category, we
   * delegate these payloads to respective handlers.
   *
   * @param message Node Message
   */
 // @Override
  public void transIncoming(Message message) {
    log(LogLevel.DEBUG, message._source.getID() + " " + message.payload.toString());

    if (message.payload instanceof TokenPayload) {
      handleTokenMessage(message._source, (TokenPayload) message.payload);
    } else if (message.payload instanceof RejectPayload) {
      handleRejectMessage(message._source, (RejectPayload) message.payload);
    } else if (message.payload instanceof IAmDonePayload) {
      handleIAmDoneMessage(message._source, (IAmDonePayload) message.payload);
    } else if (message.payload instanceof TerminatePayload) {
      handleTerminateMessage(message._source, (TerminatePayload) message.payload);
    }
  }

  private synchronized void handleTokenMessage(ProcessId source, TokenPayload payload) {
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

  private synchronized void handleRejectMessage(ProcessId source, RejectPayload payload) {
    this.rejectCount++;

    if (this.rejectCount == getNeighbours().size()) {
      bfsTree.isLeaf = true;
      send(bfsTree.parentId, new Message(getProcessId(), new IAmDonePayload(maxId)));
    }
  }

  private synchronized void handleIAmDoneMessage(ProcessId source, IAmDonePayload payload) {
    this.iAmDoneCount++;

    this.maxId = Math.max(maxId, payload.maxId);

    // I am done is sent by its children
    this.bfsTree.children.add(source);

    if (this.iAmDoneCount == getNeighbours().size()) terminate();
  }

  private synchronized void handleTerminateMessage(ProcessId source, TerminatePayload payload) {
    isTerminated = true;

    if (!bfsTree.isLeaf)
      bfsTree.children.forEach(child -> send(child, new Message(getProcessId(), payload)));
  }

  /** Terminate Announcement */
  private void terminate() {
    isTerminated = true;
    bfsTree.children.forEach(
        child -> send(child, new Message(getProcessId(), new TerminatePayload(maxId))));
  }

  private void log(LogLevel logLevel, String message) {
    if (logLevel.getValue() >= GlobalConstants.LOG_LEVEL.getValue()) System.out.println(message);
  }
}
