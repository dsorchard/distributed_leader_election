package edu.utd.dc.async.bfs.layered.layered.domain.payload;

/** Leader announces it's ID using Terminate Payload. */
public class TerminatePayload {
  public int leaderId;

  public TerminatePayload(int leaderId) {
    this.leaderId = leaderId;
  }

  @Override
  public String toString() {
    return "TerminatePayload{" + "leaderId=" + leaderId + '}';
  }
}
