package edu.utd.dc.asynch.algos.spanning_tree.bfs.layered;

import edu.utd.dc.asynch.algos.spanning_tree.bfs.layered.domain.payload.ack.NAckPayload;
import edu.utd.dc.asynch.algos.spanning_tree.bfs.layered.domain.payload.ack.PAckPayload;
import edu.utd.dc.asynch.algos.spanning_tree.bfs.layered.domain.payload.IAmDonePayload;
import edu.utd.dc.asynch.algos.spanning_tree.bfs.layered.domain.payload.NewPhasePayload;
import edu.utd.dc.asynch.algos.spanning_tree.bfs.layered.domain.payload.SearchPayload;
import edu.utd.dc.asynch.algos.spanning_tree.bfs.layered.domain.payload.TerminatePayload;
import edu.utd.dc.asynch.constants.LogLevel;
import edu.utd.dc.asynch.core.ASyncProcess;
import edu.utd.dc.asynch.core.io.sharedmemory.domain.Message;
import edu.utd.dc.asynch.core.support.ProcessId;
import edu.utd.dc.asynch.tree.TreeNode;

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
  public synchronized void handleIncoming(Message message) {

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
  private void handleSearchMessage(ProcessId source) {

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
    handleAckMessage(source);
  }

  // DONE
  private void handleNAckMessage(ProcessId source) {
    this.nAckProcessIdSet.add(source.getID());
    handleAckMessage(source);
  }

  private void handleAckMessage(ProcessId source) {
    this.leaderId = Math.max(leaderId, source.getID());

    if (pAckProcessIdSet.size() + nAckProcessIdSet.size() == getNeighbours().size()) {
      this.isNewNodeDiscovered = !pAckProcessIdSet.isEmpty();

      if (bfsTree.isRoot) {
        if (!isNewNodeDiscovered) terminate(getProcessId());
        else startNextPhase();
      } else {
        send(
            this.bfsTree.parentId,
            new Message(getProcessId(), new IAmDonePayload(isNewNodeDiscovered, leaderId)));
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

  private void handleIAmDoneMessage(ProcessId source, IAmDonePayload payload) {
    this.leaderId = Math.max(leaderId, payload.maxProcessId);

    this.iAmDoneProcessIdSet.add(source.getID());
    this.isNewNodeDiscovered = isNewNodeDiscovered || payload.isNewNodeDiscovered;

    if (this.iAmDoneProcessIdSet.size() == getNeighbours().size()) {
      if (bfsTree.isRoot) {
        if (!isNewNodeDiscovered) terminate(getProcessId());
        else startNextPhase();
      } else {
        send(
            bfsTree.parentId,
            new Message(getProcessId(), new IAmDonePayload(isNewNodeDiscovered, leaderId)));
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
                  send(getProcessId(), new Message(source, new NAckPayload()));
                } else {
                  send(neighbour, new Message(getProcessId(), new SearchPayload()));
                }
              });

    } else {
      getNeighbours()
          .forEach(
              neighbour -> {
                if (source.getID() == neighbour.getID()) {
                  send(
                      getProcessId(),
                      new Message(source, new IAmDonePayload(false, source.getID())));
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
  private void handleTerminateMessage(ProcessId source, TerminatePayload payload) {
    this.leaderId = Math.max(leaderId, payload.leaderId);
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
