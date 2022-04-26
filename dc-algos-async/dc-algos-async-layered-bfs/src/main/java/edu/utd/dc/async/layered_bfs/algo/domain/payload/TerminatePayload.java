package edu.utd.dc.async.layered_bfs.algo.domain.payload;

/**
 * Terminate message is send for the algo to terminate. On the way down (broadcast) we set the max
 * leaderId.
 */
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
