package edu.utd.dc.project2.algo.leaderelection.floodmax.domain.payload;

/** Leader announces it's ID using Terminate Payload. */
public class TerminatePayload {
  public int leaderId;

  public TerminatePayload(int leaderId) {
    this.leaderId = leaderId;
  }

  @Override
  public String toString() {
    return "TokenPayload{" + "leaderId=" + leaderId + '}';
  }
}
