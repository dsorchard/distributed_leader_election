package edu.utd.dc.synch.algo.leaderelection.floodmax.domain.payload;

/** Contains the max leader id seen so far */
public class SearchPayload {
  public int leaderId;

  public SearchPayload(int leaderId) {
    this.leaderId = leaderId;
  }

  @Override
  public String toString() {
    return "TokenPayload{" + "leaderId=" + leaderId + '}';
  }
}
