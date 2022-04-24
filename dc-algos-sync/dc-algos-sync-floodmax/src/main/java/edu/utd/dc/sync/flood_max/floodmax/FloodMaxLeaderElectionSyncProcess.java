package edu.utd.dc.sync.flood_max.floodmax;

import edu.utd.dc.common.constants.LogLevel;
import edu.utd.dc.common.domain.io.Message;
import edu.utd.dc.common.domain.support.ProcessId;
import edu.utd.dc.common.tree.TreeNode;
import edu.utd.dc.sync.core.SyncProcess;
import edu.utd.dc.sync.flood_max.floodmax.domain.payload.IAmDonePayload;
import edu.utd.dc.sync.flood_max.floodmax.domain.payload.RejectPayload;
import edu.utd.dc.sync.flood_max.floodmax.domain.payload.SearchPayload;
import edu.utd.dc.sync.flood_max.floodmax.domain.payload.TerminatePayload;

import java.util.HashSet;
import java.util.Set;

public class FloodMaxLeaderElectionSyncProcess extends SyncProcess {

  private final TreeNode<ProcessId> bfsTree;
  private final Set<ProcessId> rejectProcessIdSet;
  private final Set<ProcessId> iAmDoneProcessIdSet;

  private int leaderId;

  public FloodMaxLeaderElectionSyncProcess(ProcessId processId) {
    super(processId);

    this.leaderId = processId.getID();
    this.bfsTree = new TreeNode<>();

    this.rejectProcessIdSet = new HashSet<>();
    this.iAmDoneProcessIdSet = new HashSet<>();
  }

  /**
   * Handles reset of previous round states.
   *
   * @param roundNumber Contains the current round Number
   */
  @Override
  protected void handlePreRound(int roundNumber) {
    log(LogLevel.DEBUG, "Round #" + roundNumber);

    this.rejectProcessIdSet.clear();
    this.iAmDoneProcessIdSet.clear();
  }

  /**
   * handles incoming message in the previous round.
   *
   * <pre>
   *     NOTE: here we are using the previous round messages.
   *
   *     IN (prev), OUT ---> IN (prev), OUT --> IN (prev), OUT
   *        round 0             round 1            round 2
   * </pre>
   */
  @Override
  public void handleIncoming() {
    this.prevRoundReceivedMessages.forEach(
        message -> {
          log(LogLevel.DEBUG, "Receiver " + getProcessId() + " " + message.toString());

          if (message.payload instanceof SearchPayload) {
            handleSearchMessage(message._source, (SearchPayload) message.payload);
          } else if (message.payload instanceof RejectPayload) {
            handleRejectMessage(message._source);
          } else if (message.payload instanceof IAmDonePayload) {
            handleIAmDoneMessage(message._source);
          } else if (message.payload instanceof TerminatePayload) {
            handleTerminateMessage(message._source, (TerminatePayload) message.payload);
          }
        });
  }

  /**
   * Handles outgoing messages.
   *
   * <p>NOTE: The outgoing messages take into account, the leaderId received in the previous round.
   */
  @Override
  public void handleOutgoing() {

    getNeighbours()
        .forEach(
            neighbour -> send(neighbour, new Message(getProcessId(), new SearchPayload(leaderId))));
  }

  /**
   * If you get a search message in the previous round, you check
   *
   * <pre>
   * a. if you are having a larger leaderId,
   *    set it a the current node's id.
   *    adjust bfsTree parent
   * b. else
   *    send out a reject message
   *
   * </pre>
   *
   * @param source sender of the message
   * @param payload contains leader id from the neighbours.
   */
  private synchronized void handleSearchMessage(ProcessId source, SearchPayload payload) {
    if (payload.leaderId > leaderId) {
      // 1. set max ID
      leaderId = payload.leaderId;

      // 2. set tree relation
      bfsTree.parentId = source;

    } else {
      send(source, new Message(getProcessId(), new RejectPayload()));
    }
  }

  /**
   * If you get a reject message in the previous round, you
   *
   * <pre>
   *  add it to the reject set.
   *
   *  If you get reject message from all your neighbours, then
   *    you say, that you are a leaf.
   *    send IAM done to your parent.
   * </pre>
   */
  private synchronized void handleRejectMessage(ProcessId source) {
    this.rejectProcessIdSet.add(source);

    if (this.rejectProcessIdSet.size() == getNeighbours().size()) {
      bfsTree.isLeaf = true;
      send(bfsTree.parentId, new Message(getProcessId(), new IAmDonePayload()));
    }
  }

  /**
   * If you get an IAmDone message in the previous round, you
   *
   * <pre>
   *  1. add it to the iAmDone set.
   *  2. add the source as child in your bfs tree.
   *  if all the neighbours send an I AM Done
   *    if it is a root, then terminate
   *    else send IAM Done to your parent as well.
   * </pre>
   */
  private synchronized void handleIAmDoneMessage(ProcessId source) {
    this.iAmDoneProcessIdSet.add(source);
    this.bfsTree.children.add(source);

    if (this.iAmDoneProcessIdSet.size() == getNeighbours().size()) {
      if (bfsTree.parentId == null) terminate(getProcessId());
      else send(bfsTree.parentId, new Message(getProcessId(), new IAmDonePayload()));
    }
  }

  /**
   * If you get a TerminateMessage in the previous round, the leader is already elected and you need
   * to have that leader information. Also, with that you should stop your execution.
   *
   * <pre>
   *  1. set your leader id
   *  2. invoke termination procedure.
   * </pre>
   */
  private synchronized void handleTerminateMessage(ProcessId source, TerminatePayload payload) {
    this.leaderId = payload.leaderId;
    terminate(source);
  }

  /**
   * if termination is invoked
   *
   * <pre>
   *  1. set terminated flag
   *  2. send terminate message to the neighbours, except to the ones who send you the terminate message
   * </pre>
   *
   * @param source From whom did you get the termination.
   *     <p>Note: for simplicity sake we are sending the leader's parent id, when leader invokes
   *     terminate. This will not affect the filter clause
   */
  private void terminate(ProcessId source) {
    if (isTerminated()) return;

    bfsTree.children.stream()
        .filter(child -> child.getID() != source.getID())
        .forEach(child -> send(child, new Message(getProcessId(), new TerminatePayload(leaderId))));

    setTerminated(true);
  }

  /** Returns the current leader ID */
  public int getLeaderId() {
    return leaderId;
  }
}
