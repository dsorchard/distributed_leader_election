package edu.utd.dc.sync.core;

import edu.utd.dc.common.constants.LogLevel;
import edu.utd.dc.common.design_pattern.listener.Listener;
import edu.utd.dc.common.domain.io.Message;
import edu.utd.dc.common.domain.support.ProcessId;
import edu.utd.dc.sync.flood_max.floodmax.FloodMaxLeaderElectionManager;
import edu.utd.dc.sync.flood_max.constants.GlobalConstants;
import edu.utd.dc.sync.flood_max.core.io.sharedmemory.SharedMemoryBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Every algorithm node will extend this class and implement its functionality.
 *
 * <p>Uses Template pattern.
 */
public abstract class SyncProcess implements Listener, Runnable {

  private final ProcessId processId;
  private final List<ProcessId> neighbours;

  private final List<Message> currRoundReceivedMessages;
  protected List<Message> prevRoundReceivedMessages;

  private boolean canStartRound;
  private boolean isTerminated;
  private int roundNumber;

  public SyncProcess(ProcessId processId) {
    this.isTerminated = false;
    this.canStartRound = false;

    this.neighbours = new ArrayList<>();
    this.processId = processId;

    this.roundNumber = 0;
    this.currRoundReceivedMessages = new ArrayList<>();
    this.prevRoundReceivedMessages = new ArrayList<>();
  }

  /**
   * Invoked by the {@link FloodMaxLeaderElectionManager#electLeader()} after a fixed interval (Sync
   * Clock).
   */
  public void enableNextRound() {
    this.canStartRound = true;

    // This is to avoid concurrent modification
    this.prevRoundReceivedMessages.clear();
    this.prevRoundReceivedMessages.addAll(currRoundReceivedMessages);

    this.currRoundReceivedMessages.clear();

    syncNotify();
  }

  /**
   * Endless while loop util terminated. If you can start a new round, it invokes {@link
   * #handleOutgoing()} and then goes to {@link #syncWait()}. It will be in wait state untill {@link
   * #syncNotify()} is invoked from the {@link #canStartRound}.
   */
  @Override
  public void run() {

    while (!isTerminated) {

      if (this.canStartRound) startNextRound();
      this.canStartRound = false;

      syncWait();
    }
  }

  /** Template pattern. */
  private void startNextRound() {

    handlePreRound(roundNumber);
    handleIncoming();
    handleOutgoing();

    roundNumber++;
  }

  protected abstract void handlePreRound(int roundNumber);

  protected abstract void handleOutgoing();

  protected abstract void handleIncoming();

  protected void send(ProcessId destinationId, Message message) {
    SharedMemoryBus.send(destinationId, message);
  }

  @Override
  public void onReceive(Message message) {
    this.currRoundReceivedMessages.add(message);
  }

  public void addNeighbour(SyncProcess neighbourProcess) {
    this.neighbours.add(neighbourProcess.processId);
    SharedMemoryBus.register(this.processId, neighbourProcess.processId, neighbourProcess);
  }

  public ProcessId getProcessId() {
    return processId;
  }

  public List<ProcessId> getNeighbours() {
    return neighbours;
  }

  public boolean isTerminated() {
    return isTerminated;
  }

  public void setTerminated(boolean terminated) {
    isTerminated = terminated;
  }

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

  protected void log(LogLevel logLevel, String message) {
    if (logLevel.getValue() >= GlobalConstants.LOG_LEVEL.getValue()) System.out.println(message);
  }
}
