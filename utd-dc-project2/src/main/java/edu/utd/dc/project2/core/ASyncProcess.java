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

  private boolean isTerminated;

  public ASyncProcess(ProcessId processId) {
    this.isTerminated = false;

    this.neighbours = new ArrayList<>();
    this.processId = processId;
  }

  /**
   * Invoked by the {@link LayeredBfsManager#buildBFSTree()} after a fixed interval (Sync Clock).
   */

  /**
   * Endless while loop util terminated. If you can start a new round, it invokes {@link
   * #handleOutgoing()} and then goes to {@link #syncWait()}. It will be in wait state untill {@link
   * #syncNotify()} is invoked from the {@link #canStartNextPhase}.
   */
  @Override
  public void run() {
    while (!isTerminated) {
      syncWait();
    }
  }

  public abstract void initiate();

  protected abstract void handleIncoming(Message message);

  protected void send(ProcessId destinationId, Message message) {
    SharedMemoryBus.send(destinationId, message);
  }

  protected void send(ProcessId destinationId, Message message, int delay) {
    SharedMemoryBus.send(destinationId, message, delay);
  }

  @Override
  public void onReceive(Message message) {
    syncNotify();
    this.handleIncoming(message);
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
    syncNotify();
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
