package edu.utd.dc.project2.algo.bfs.layered;

import edu.utd.dc.project2.algo.bfs.layered.domain.payload.*;
import edu.utd.dc.project2.algo.bfs.layered.domain.payload.ack.NAckPayload;
import edu.utd.dc.project2.algo.bfs.layered.domain.payload.ack.PAckPayload;
import edu.utd.dc.project2.constants.LogLevel;
import edu.utd.dc.project2.core.ASyncProcess;
import edu.utd.dc.project2.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project2.core.support.ProcessId;
import edu.utd.dc.project2.tree.TreeNode;
import edu.utd.dc.project2.utils.RandomUtils;

import java.util.HashSet;
import java.util.Set;

public class LayeredBfsASyncProcess extends ASyncProcess {

  private final TreeNode<ProcessId> bfsTree;
  private final Set<ProcessId> iAmDoneProcessIdSet;

  private final Set<ProcessId> pAckProcessIdSet;
  private final Set<ProcessId> nAckProcessIdSet;

  private int depthExplored;
  private int leaderId;

  private boolean isNewNodeDiscovered = false;

  int delay = RandomUtils.valueBetween(1, 12);

  public LayeredBfsASyncProcess(ProcessId processId) {
    super(processId);

    this.leaderId = processId.getID();
    this.bfsTree = new TreeNode<>();

    this.iAmDoneProcessIdSet = new HashSet<>();
    this.pAckProcessIdSet = new HashSet<>();
    this.nAckProcessIdSet = new HashSet<>();

    this.depthExplored = 0;
  }

  protected void handlePrePhase(int phaseNumber) {
    this.iAmDoneProcessIdSet.clear();
  }

  @Override
  public void handleIncoming(Message message) {

    log(LogLevel.DEBUG, "Receiver " + getProcessId() + " " + message.toString());

    if (message.payload instanceof NewPhasePayload) {
      handleNewPhaseMessage(message._source, (NewPhasePayload) message.payload);
    } else if (message.payload instanceof SearchPayload) {
      handleSearchMessage(message._source);
    } else if (message.payload instanceof PAckPayload) {
      handlePAckMessage(message._source);
    } else if (message.payload instanceof NAckPayload) {
      handleNAckMessage(message._source);
    } else if (message.payload instanceof IAmDonePayload) {
      handleIAmDoneMessage(message._source, (IAmDonePayload) message.payload);
    } else if (message.payload instanceof TerminatePayload) {
      handleTerminateMessage(message._source, (TerminatePayload) message.payload);
    }
  }

  // DONE
  @Override
  public void initiate() {
    this.bfsTree.isRoot = true;

    getNeighbours()
        .forEach(
            neighbour -> send(neighbour, new Message(getProcessId(), new SearchPayload()), delay));
  }

  // DONE
  private synchronized void handleSearchMessage(ProcessId source) {
    if (!bfsTree.isRoot && bfsTree.parentId == null) {
      bfsTree.parentId = source;
      send(source, new Message(getProcessId(), new PAckPayload()), delay);
    } else {
      send(source, new Message(getProcessId(), new NAckPayload()), delay);
    }
  }

  // DONE
  private void handlePAckMessage(ProcessId source) {
    this.bfsTree.children.add(source);

    this.pAckProcessIdSet.add(source);

    if (pAckProcessIdSet.size() + nAckProcessIdSet.size() == getNeighbours().size()) {

      if (bfsTree.isRoot) {
        startNextPhase();
      } else {

        this.isNewNodeDiscovered = !pAckProcessIdSet.isEmpty();
        send(
            this.bfsTree.parentId,
            new Message(getProcessId(), new IAmDonePayload(isNewNodeDiscovered)),
            delay);
      }

      reset();
    }
  }

  // DONE
  private synchronized void handleNAckMessage(ProcessId source) {
    this.nAckProcessIdSet.add(source);

    if (pAckProcessIdSet.size() + nAckProcessIdSet.size() == getNeighbours().size()) {

      if (bfsTree.isRoot) {
        startNextPhase();
      } else {

        this.isNewNodeDiscovered = !pAckProcessIdSet.isEmpty();
        send(
            this.bfsTree.parentId,
            new Message(getProcessId(), new IAmDonePayload(isNewNodeDiscovered)),
            delay);
      }

      reset();
    }
  }

  private void reset() {
    pAckProcessIdSet.clear();
    nAckProcessIdSet.clear();
    iAmDoneProcessIdSet.clear();

    isNewNodeDiscovered = false;
  }

  private synchronized void handleIAmDoneMessage(ProcessId source, IAmDonePayload payload) {
    this.iAmDoneProcessIdSet.add(source);
    this.isNewNodeDiscovered = isNewNodeDiscovered || payload.isNewNodeDiscovered; // TODO:

    if (this.iAmDoneProcessIdSet.size() == getNeighbours().size()) {
      if (bfsTree.isRoot) {
        if (!isNewNodeDiscovered) terminate(getProcessId());
        else startNextPhase();
      } else {
        send(
            bfsTree.parentId, new Message(getProcessId(), new IAmDonePayload(isNewNodeDiscovered)));
      }

      reset();
      isNewNodeDiscovered = false; // TODO:
    }
  }

  private void startNextPhase() {
    this.depthExplored++;

    getNeighbours()
        .forEach(
            neighbour ->
                send(
                    neighbour,
                    new Message(getProcessId(), new NewPhasePayload(this.depthExplored - 1)),
                    delay));
  }

  private void handleNewPhaseMessage(ProcessId source, NewPhasePayload payload) {

    if (payload.depth == 0) {

      getNeighbours()
          .forEach(
              neighbour -> {
                if (neighbour.getID() != source.getID())
                  send(neighbour, new Message(getProcessId(), new SearchPayload()), delay);
              });

    } else {
      getNeighbours()
          .forEach(
              neighbour -> {
                if (neighbour.getID() != source.getID())
                  send(
                      neighbour,
                      new Message(getProcessId(), new NewPhasePayload(payload.depth - 1)),
                      delay);
              });
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
