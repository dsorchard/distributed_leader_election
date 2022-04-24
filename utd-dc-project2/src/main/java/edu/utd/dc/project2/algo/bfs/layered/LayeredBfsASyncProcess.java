package edu.utd.dc.project2.algo.bfs.layered;

import edu.utd.dc.project2.algo.bfs.layered.domain.payload.*;
import edu.utd.dc.project2.algo.bfs.layered.domain.payload.ack.NAckPayload;
import edu.utd.dc.project2.algo.bfs.layered.domain.payload.ack.PAckPayload;
import edu.utd.dc.project2.constants.LogLevel;
import edu.utd.dc.project2.core.ASyncProcess;
import edu.utd.dc.project2.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project2.core.support.ProcessId;
import edu.utd.dc.project2.tree.TreeNode;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LayeredBfsASyncProcess extends ASyncProcess {

  private final TreeNode<ProcessId> bfsTree;
  private final Set<Integer> iAmDoneProcessIdSet;
  private final Set<Integer> pAckProcessIdSet;
  private final Set<Integer> nAckProcessIdSet;

  private int depthExplored;
  private int leaderId;

  private boolean isNewNodeDiscovered = false;

  public LayeredBfsASyncProcess(ProcessId processId) {
    super(processId);

    this.leaderId = processId.getID();
    this.bfsTree = new TreeNode<>();

    this.iAmDoneProcessIdSet = ConcurrentHashMap.newKeySet();
    this.pAckProcessIdSet = ConcurrentHashMap.newKeySet();
    this.nAckProcessIdSet = ConcurrentHashMap.newKeySet();

    this.depthExplored = 0;
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
        .forEach(neighbour -> send(neighbour, new Message(getProcessId(), new SearchPayload())));
  }

  // DONE
  private synchronized void handleSearchMessage(ProcessId source) {
    if (!bfsTree.isRoot && bfsTree.parentId == null) {
      bfsTree.parentId = source;
      send(source, new Message(getProcessId(), new PAckPayload()));
    } else {
      send(source, new Message(getProcessId(), new NAckPayload()));
    }
  }

  // DONE
  private void handlePAckMessage(ProcessId source) {
    this.bfsTree.children.add(source);
    this.pAckProcessIdSet.add(source.getID());
    handleAckMessage();
  }

  // DONE
  private synchronized void handleNAckMessage(ProcessId source) {
    this.nAckProcessIdSet.add(source.getID());
    handleAckMessage();
  }

  private void handleAckMessage() {
    if (pAckProcessIdSet.size() + nAckProcessIdSet.size() == getNeighbours().size()) {
      this.isNewNodeDiscovered = !pAckProcessIdSet.isEmpty();

      if (bfsTree.isRoot) {
        if (!isNewNodeDiscovered) terminate(getProcessId());
        else startNextPhase();
      } else {
        send(
            this.bfsTree.parentId,
            new Message(getProcessId(), new IAmDonePayload(isNewNodeDiscovered)));
      }

      resetStateVariables();
    }
  }

  private void resetStateVariables() {
    pAckProcessIdSet.clear();
    nAckProcessIdSet.clear();
    iAmDoneProcessIdSet.clear();

    isNewNodeDiscovered = false;
  }

  private synchronized void handleIAmDoneMessage(ProcessId source, IAmDonePayload payload) {
    this.iAmDoneProcessIdSet.add(source.getID());
    this.isNewNodeDiscovered = isNewNodeDiscovered || payload.isNewNodeDiscovered;

    if (this.iAmDoneProcessIdSet.size() == getNeighbours().size()) {
      if (bfsTree.isRoot) {
        if (!isNewNodeDiscovered) terminate(getProcessId());
        else startNextPhase();
      } else {
        send(
            bfsTree.parentId, new Message(getProcessId(), new IAmDonePayload(isNewNodeDiscovered)));
      }

      resetStateVariables();
    }
  }

  private void startNextPhase() {
    this.depthExplored++;

    getNeighbours()
        .forEach(
            neighbour ->
                send(
                    neighbour,
                    new Message(getProcessId(), new NewPhasePayload(this.depthExplored - 1))));
  }

  private void handleNewPhaseMessage(ProcessId source, NewPhasePayload payload) {

    if (payload.depth == 0) {

      getNeighbours()
          .forEach(
              neighbour -> {
                if (source.getID() == neighbour.getID()) {
                  nAckProcessIdSet.add(source.getID());
                } else {
                  send(neighbour, new Message(getProcessId(), new SearchPayload()));
                }
              });

    } else {
      getNeighbours()
          .forEach(
              neighbour -> {
                if (source.getID() == neighbour.getID()) {
                  iAmDoneProcessIdSet.add(source.getID());
                } else {
                  send(
                      neighbour,
                      new Message(getProcessId(), new NewPhasePayload(payload.depth - 1)));
                }
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

    System.out.println(
        "Output : " + "Process ID = " + getProcessId().getID() + " Children = " + bfsTree.children);

    setTerminated(true);
  }

  /** Returns the current leader ID */
  public int getLeaderId() {
    return leaderId;
  }
}
