package edu.utd.dc.project0.algo.leaderelection.floodmax.domain.payload;

public class TerminatePayload {
  public int leaderId;

  public TerminatePayload(int leaderId) {
    this.leaderId = leaderId;
  }

  @Override
  public String toString() {
    return "TokenPayload{" + "maxId=" + leaderId + '}';
  }
}
