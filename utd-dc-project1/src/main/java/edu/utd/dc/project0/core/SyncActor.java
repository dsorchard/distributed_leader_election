package edu.utd.dc.project0.core;

import edu.utd.dc.project0.algo.leaderelection.floodmax.FloodMaxLeaderElectionManager;
import edu.utd.dc.project0.core.io.sharedmemory.Listener;
import edu.utd.dc.project0.core.io.sharedmemory.SharedMemoryBus;
import edu.utd.dc.project0.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project0.core.support.ProcessId;

import java.util.ArrayList;
import java.util.List;

/**
 * Every algorithm node will extend this class and implement its functionality.
 *
 * <p>Uses Template pattern.
 */
public abstract class SyncActor implements Listener, Runnable {

  private final ProcessId own;
  private final List<ProcessId> neighbours;
  protected List<Message> messageQueue;

  protected boolean canStartRound;
  protected boolean isTerminated;

  public SyncActor(ProcessId processId) {
    this.isTerminated = false;
    this.canStartRound = false;

    this.neighbours = new ArrayList<>();
    this.own = processId;

    this.messageQueue = new ArrayList<>();
  }

  public void send(ProcessId destinationId, Message message) {
    SharedMemoryBus.send(destinationId, message);
  }

  /**
   * Endless while loop util terminated. If you can start a new round, it invokes {@link
   * #msgsOutgoing()} and then goes to {@link #syncWait()}. It will be in wait state untill {@link
   * #syncNotify()} is invoked from the {@link #canStartRound}.
   */
  @Override
  public void run() {

    while (!isTerminated) {

      if (this.canStartRound) msgsOutgoing();
      this.canStartRound = false;

      syncWait();
    }
  }

  /**
   * Invoked by the {@link FloodMaxLeaderElectionManager#electLeader()} after a fixed interval (Sync
   * Clock).
   */
  public void enableNextRound() {
    this.canStartRound = true;
    this.messageQueue.clear();
    syncNotify();
  }

  @Override
  public void onReceive(Message message) {
    this.messageQueue.add(message);
  }

  public abstract void msgsOutgoing();

  public void addNeighbour(SyncActor neighbourProcess) {
    this.neighbours.add(neighbourProcess.own);
    SharedMemoryBus.register(this.own, neighbourProcess.own, neighbourProcess);
  }

  public ProcessId getProcessId() {
    return own;
  }

  public List<ProcessId> getNeighbours() {
    return neighbours;
  }

  //  /** This will usually contain an endless while loop with wait, to mimic server node. */
  //  public abstract void start();

  /**
   * Responsible for handling received message
   *
   * @param message Node Message
   */
  public abstract void transIncoming();

  /** Pause using locks */
  private void syncWait() {
    try {
      synchronized (this) {
        wait();
      }
    } catch (InterruptedException ignored) {
    }
  }

  /** Resume using locks */
  private void syncNotify() {
    synchronized (this) {
      notify();
    }
  }
}
