package edu.utd.dc.sync.flood_max.algo.domain.payload;

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
