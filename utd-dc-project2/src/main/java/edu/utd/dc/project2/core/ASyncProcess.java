package edu.utd.dc.project2.core;

import edu.utd.dc.project2.algo.bfs.layered.LayeredBfsManager;
import edu.utd.dc.project2.constants.GlobalConstants;
import edu.utd.dc.project2.constants.LogLevel;
import edu.utd.dc.project2.core.io.sharedmemory.Listener;
import edu.utd.dc.project2.core.io.sharedmemory.SharedMemoryBus;
import edu.utd.dc.project2.core.io.sharedmemory.domain.Message;
import edu.utd.dc.project2.core.support.ProcessId;

import java.util.ArrayList;
import java.util.List;

/**
 * Every algorithm node will extend this class and implement its functionality.
 *
 * <p>Uses Template pattern.
 */
public abstract class ASyncProcess implements Listener, Runnable {

  private final ProcessId processId;
  private final List<ProcessId> neighbours;

  private final List<Message> currRoundReceivedMessages;
  protected List<Message> prevRoundReceivedMessages;

  private boolean canStartNextPhase;
  private boolean isTerminated;
  private int roundNumber;

  public ASyncProcess(ProcessId processId) {
    this.isTerminated = false;
    this.canStartNextPhase = false;

    this.neighbours = new ArrayList<>();
    this.processId = processId;

    this.roundNumber = 0;
    this.currRoundReceivedMessages = new ArrayList<>();
    this.prevRoundReceivedMessages = new ArrayList<>();
  }

  /**
   * Invoked by the {@link LayeredBfsManager#buildBFSTree()} after a fixed interval (Sync Clock).
   */
  public void enableNextPhase() {
    this.canStartNextPhase = true;

    // This is to avoid concurrent modification
    this.prevRoundReceivedMessages.clear();
    this.prevRoundReceivedMessages.addAll(currRoundReceivedMessages);

    this.currRoundReceivedMessages.clear();

    syncNotify();
  }

  /**
   * Endless while loop util terminated. If you can start a new round, it invokes {@link
   * #handleOutgoing()} and then goes to {@link #syncWait()}. It will be in wait state untill {@link
   * #syncNotify()} is invoked from the {@link #canStartNextPhase}.
   */
  @Override
  public void run() {

    while (!isTerminated) {

      if (this.canStartNextPhase) startNextPhase();
      this.canStartNextPhase = false;

      syncWait();
    }
  }

  /** Template pattern. */
  private void startNextPhase() {

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

  protected void send(ProcessId destinationId, Message message, int delay) {
    SharedMemoryBus.send(destinationId, message, delay);
  }

  @Override
  public void onReceive(Message message) {
    this.currRoundReceivedMessages.add(message);
  }

  public void addNeighbour(ASyncProcess neighbourProcess) {
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
