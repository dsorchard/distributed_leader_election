package edu.utd.dc.async.layered_bfs.algo;

import edu.utd.dc.async.layered_bfs.algo.domain.payload.IAmDonePayload;
import edu.utd.dc.async.layered_bfs.algo.domain.payload.TerminatePayload;
import edu.utd.dc.async.layered_bfs.algo.domain.payload.ack.NAckPayload;
import edu.utd.dc.async.layered_bfs.algo.domain.payload.ack.PAckPayload;
import edu.utd.dc.async.layered_bfs.algo.domain.payload.NewPhasePayload;
import edu.utd.dc.async.layered_bfs.algo.domain.payload.SearchPayload;
import edu.utd.dc.async.layered_bfs.constants.GlobalConstants;
import edu.utd.dc.async.core.ASyncProcess;
import edu.utd.dc.common.constants.LogLevel;
import edu.utd.dc.common.domain.io.Message;
import edu.utd.dc.common.domain.support.ProcessId;
import edu.utd.dc.common.tree.TreeNode;

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

  /**
   * Called by one process to start the algorithm.
   *
   * <p>In our current algo, in phase 1: we send search message to every neighbour.
   */
  @Override
  public void initiate() {
    this.bfsTree.isRoot = true;

    getNeighbours()
        .forEach(neighbour -> send(neighbour, new Message(getProcessId(), new SearchPayload())));
  }

  /**
   * Search message to neighbour either gets a response of NACK (if parent is set) or PACK (if
   * parent is not set).
   */
  private void handleSearchMessage(ProcessId source) {

    if (!bfsTree.isRoot && bfsTree.parentId == null) {
      bfsTree.parentId = source;
      send(source, new Message(getProcessId(), new PAckPayload()));
    } else {
      send(source, new Message(getProcessId(), new NAckPayload()));
    }
  }

  /** PACK is used by the reciever to mark it as child. */
  private void handlePAckMessage(ProcessId source) {
    this.bfsTree.children.add(source);
    this.pAckProcessIdSet.add(source.getID());
    handleAckMessage(source);
  }

  /** NACK is just used for maintaining the count. */
  private void handleNAckMessage(ProcessId source) {
    this.nAckProcessIdSet.add(source.getID());
    handleAckMessage(source);
  }

  /**
   * Any ACK message when received is checked with the following condition:
   *
   * <p>Did I receive ack (NACK or PACK) from all the neighbours? If I am root, and I didn't find a
   * new neighbour in this phase, terminate. Else if I am root, and I found a new neighbour, go to
   * next phase. If I am not root, convergecast with IAM done message to the parent.
   *
   * <p>Either way, clear my state after I am done recieving the ACK from all my children.
   */
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

  /** Resets PACK, NACK & IAM Done counters. NewNode flag is also reset. */
  private void resetStateVariables() {
    pAckProcessIdSet.clear();
    nAckProcessIdSet.clear();
    iAmDoneProcessIdSet.clear();

    isNewNodeDiscovered = false;
  }

  /**
   * On convergecast, If I recieve all the IAM done messages, I will either terminate (if I a root
   * and condition met) or send I AM done to my parent.
   *
   * <p>When I collect all my child IAM done messages, I check if any one got a new neighbour (using
   * flag) & also, update my leader id with the max process id received so far. The following
   * knowledge is send to my parent.
   */
  private void handleIAmDoneMessage(ProcessId source, IAmDonePayload payload) {
    this.leaderId = Math.max(leaderId, payload.maxProcessId);

    this.iAmDoneProcessIdSet.add(source.getID());
    this.isNewNodeDiscovered = isNewNodeDiscovered || payload.isNewNodeDiscovered;

    if (this.iAmDoneProcessIdSet.size() == bfsTree.children.size()) {
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

  /**
   * When I have a new neighbour discovered in the previous phase, I send out a new phase message
   * from the root.
   */
  private void startNextPhase() {
    this.depthExplored++;

    getNeighbours()
        .forEach(
            neighbour ->
                send(
                    neighbour,
                    new Message(getProcessId(), new NewPhasePayload(this.depthExplored - 1))));
  }

  /**
   * A new phase message is propogated to the k (discovered) depth (we are decrementing the depth
   * inside the message). Upon reaching the k'th depth, we send a search message to explore the
   * children.
   *
   * <p>NOTE 1: To make things simple (handling : neighbour count for root vs neighbour cout for
   * other node without source) we are simulating a recieved NACK message from the source. We are
   * not sending New Phase to the source, but we are simulating that, we will recieved a NACK
   * message from sender to avoid complications with neighbour count check.
   *
   * <p>NOTE 2: We are sending NewPhase via BFS tree created so far.
   */
  private void handleNewPhaseMessage(ProcessId source, NewPhasePayload payload) {

    if (payload.depth == 0) {

      getNeighbours()
          .forEach(
              neighbour -> {
                if (source.getID() == neighbour.getID()) {

                  // Simulating NACK message from source to simiplify neighbour count check.
                  send(getProcessId(), new Message(source, new NAckPayload()));
                } else {
                  send(neighbour, new Message(getProcessId(), new SearchPayload()));
                }
              });

    } else {

      if (bfsTree.children.isEmpty()) {
        send(source, new Message(getProcessId(), new IAmDonePayload(false, leaderId)));
      } else {
        bfsTree.children.forEach(
            neighbour ->
                send(
                    neighbour,
                    new Message(getProcessId(), new NewPhasePayload(payload.depth - 1))));
      }
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

  protected void log(LogLevel logLevel, String message) {
    if (logLevel.getValue() >= GlobalConstants.LOG_LEVEL.getValue()) System.out.println(message);
  }
}
