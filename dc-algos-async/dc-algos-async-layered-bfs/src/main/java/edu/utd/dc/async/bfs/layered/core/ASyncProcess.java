package edu.utd.dc.async.bfs.layered.core;

import edu.utd.dc.async.bfs.layered.constants.GlobalConstants;

import edu.utd.dc.async.bfs.layered.core.io.sharedmemory.SharedMemoryBus;

import edu.utd.dc.common.constants.LogLevel;
import edu.utd.dc.common.design_pattern.listener.Listener;
import edu.utd.dc.common.domain.io.Message;
import edu.utd.dc.common.domain.support.ProcessId;
import edu.utd.dc.common.utils.RandomUtils;

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

  @Override
  public void run() {
    while (!isTerminated) syncWait();
  }

  public abstract void initiate();

  protected abstract void handleIncoming(Message message);

  protected void send(ProcessId destinationId, Message message) {
    int delay = RandomUtils.valueBetween(1, 12);
    SharedMemoryBus.send(destinationId, message, delay);
  }

  @Override
  public void onReceive(Message message) {
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
